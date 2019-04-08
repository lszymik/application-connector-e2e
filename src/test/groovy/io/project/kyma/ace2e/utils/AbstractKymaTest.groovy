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

    String serviceInstance = "test-service-instance-e2e"

    String eventTopic = "exampleEvent"

    String lambdaFunction = "test-lambda-e2e"

    String subscription = "test-subscription-e2e"

    K8SClient k8SClient = new K8SClient(EnvironmentConfig.kubeConfig)

    APIRegistryClient apiRegistryClient

    EventServiceClient eventClient

    CounterServiceClient counterClient

    SharedSource() {
        connectTestSuiteToKyma()
        setupRestClients()

        cleanUpCluster()

        prepareTestApplication()
    }

    private void prepareTestApplication() {
        def appCR = TestHelper.createApplicationCR(applicationName)
        k8SClient.createApplication(appCR)

        Awaitility.awaitUntilWithResult({
            final Application app = (Application) k8SClient.getApplication(applicationName, KymaNames.INTEGRATION_NAMESPACE)
            app?.status?.installationStatus?.status == KymaNames.STATUS_DEPLOYED
        }, 10, 120)
    }

    private def cleanUpCluster() {
        k8SClient.deleteSubscription(subscription, KymaNames.PRODUCTION_NAMESPACE)
        k8SClient.deleteLambdaFunction(lambdaFunction, KymaNames.PRODUCTION_NAMESPACE)
        k8SClient.deleteServiceInstance(serviceInstance, KymaNames.PRODUCTION_NAMESPACE)
        k8SClient.deleteTestApplications()
    }


    private def connectTestSuiteToKyma() {
        new CertificateManager(k8SClient: k8SClient, application: applicationName, namespace: KymaNames.INTEGRATION_NAMESPACE)
                .setupCertificateInKeyStore()
    }

    private def setupRestClients() {
        final String gatewayPath = EnvironmentConfig.nodePort != null ? "https://gateway.${EnvironmentConfig.domain}:${EnvironmentConfig.nodePort}" : "https://gateway.${EnvironmentConfig.domain}"
        def counterServicePath = "https://counter-service.${EnvironmentConfig.domain}"

        def securedRestClient = new RestClientWithClientCert(gatewayPath, EnvironmentConfig.jksStoreFile.toString(), EnvironmentConfig.JSK_STORE_PASSWORD)
        apiRegistryClient = new APIRegistryClient(securedRestClient)
        eventClient = new EventServiceClient(securedRestClient)

        counterClient = new CounterServiceClient(counterServicePath)
    }
}