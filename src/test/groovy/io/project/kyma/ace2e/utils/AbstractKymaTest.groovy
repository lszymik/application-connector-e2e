package io.project.kyma.ace2e.utils

import io.project.kyma.ace2e.model.k8s.Application
import spock.lang.Specification

/**
 * Abstract class for a singleton purpose.
 */
abstract class AbstractKymaTest extends Specification {
    def static sharedSource = new SharedSource()
}


/**
 * Shared resources which are used by the multiple tests.
 * It is for execution time efficiency or the sake of simplicity.
 *
 */
class SharedSource {

    String applicationName = "e2e-test-app-" + KymaNames.randomString

    K8SClient k8SClient = new K8SClient(EnvironmentConfig.kubeConfig)

    APIRegistryClient apiRegistryClient

    SharedSource() {
        println "Created shared source. Its our heavy resource!"
        connectTestSuiteToKyma()
        apiRegistryClient = setupKymaClients()

        def appCR = TestHelper.createApplicationCR(applicationName)
        k8SClient.createApplication(appCR)

        Awaitility.awaitUntilWithResult({
            final Application app = (Application) k8SClient.getApplication(applicationName, KymaNames.INTEGRATION_NAMESPACE)
            app?.status?.installationStatus?.status == KymaNames.STATUS_DEPLOYED
        }, 10, 30)
    }

    private def connectTestSuiteToKyma() {
        new CertificateManager(k8SClient: k8SClient, application: applicationName, namespace: KymaNames.INTEGRATION_NAMESPACE)
                .setupCertificateInKeyStore()
    }

    private static def setupKymaClients() {
        final String basePath = EnvironmentConfig.nodePort != null ? "https://gateway.${EnvironmentConfig.domain}:${EnvironmentConfig.nodePort}" : "https://gateway.${EnvironmentConfig.domain}"

        def kymaRestClient = new RestClientWithClientCert(basePath, EnvironmentConfig.jksStoreFile.toString(), EnvironmentConfig.JSK_STORE_PASSWORD)
        return new APIRegistryClient(kymaRestClient)
    }
}