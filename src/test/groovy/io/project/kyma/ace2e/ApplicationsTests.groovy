package io.project.kyma.ace2e


import io.project.kyma.ace2e.model.k8s.Application
import io.project.kyma.ace2e.model.k8s.ApplicationMapping
import io.project.kyma.ace2e.utils.*

import static org.apache.http.HttpStatus.SC_OK

class ApplicationsTests extends AbstractKymaTest {

    public static final String DESCRIPTION = "Test application"

    public static final Map<String, String> LABELS = ['kind': 'production', 'region': 'eu']

    def cleanupSpec() {
        cleanUpTestApplication()
    }

    def "createAnApplication"() {
        given:
        def appCR = TestHelper.createApplicationCR(sharedSource.applicationName, DESCRIPTION, LABELS)

        when:
        sharedSource.k8SClient.createApplication(appCR)

        then:
        Awaitility.awaitUntilWithResult({
            final Application app = (Application) sharedSource.k8SClient.getApplication(sharedSource.applicationName, KymaNames.INTEGRATION_NAMESPACE)
            app?.status?.installationStatus?.status == STATUS_DEPLOYED
        }, 10, 30)

        when:
        def app = (Application) sharedSource.k8SClient.getApplication(sharedSource.applicationName, KymaNames.INTEGRATION_NAMESPACE)
        def spec = app.spec

        then:
        spec.description == DESCRIPTION
        spec.labels.get("region") == "eu"
        spec.labels.get("kind") == "production"

    }

    def "bindApplicationToEnvironment"() {
        when:
        def result = sharedSource.k8SClient.bindApplicationToNamespace(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE)

        then:
        result.metadata.get("name") == sharedSource.applicationName

        when:
        ApplicationMapping mapping = (ApplicationMapping) sharedSource.k8SClient.getApplicationMapping(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE)

        then:
        mapping.metadata.name == sharedSource.applicationName
    }

    def "listAllServices"() {
        when:
        def result = sharedSource.apiRegistryClient.getServices(sharedSource.applicationName)

        then:
        result.status == SC_OK
        result.data.size() == 0
    }

    private static Object cleanUpTestApplication() {
        sharedSource.k8SClient.deleteApplication(sharedSource.applicationName, KymaNames.INTEGRATION_NAMESPACE)
    }
}
