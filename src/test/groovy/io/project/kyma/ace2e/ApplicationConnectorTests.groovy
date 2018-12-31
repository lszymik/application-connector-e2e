package io.project.kyma.ace2e

import io.project.kyma.ace2e.model.Application
import io.project.kyma.ace2e.model.Metadata
import io.project.kyma.ace2e.model.Spec
import io.project.kyma.ace2e.utils.CertificateManager
import io.project.kyma.ace2e.utils.EnvironmentConfig
import io.project.kyma.ace2e.utils.K8SClient
import io.project.kyma.ace2e.utils.MetadataClient
import spock.lang.Shared
import spock.lang.Specification
import static org.awaitility.Awaitility.*
import static java.util.concurrent.TimeUnit.*

class ApplicationConnectorTests extends Specification {

    @Shared MetadataClient metadataClient
    @Shared K8SClient k8SClient = new K8SClient(EnvironmentConfig.kubeConfig)
    @Shared Application app = newTestApp()

    @Shared def keystorePass = ""


    def setupSpec() {
        println "Starting test"
        k8SClient.createApplication(app)
        await().atMost(10, SECONDS).until{
			k8SClient.applicationExists(app.metadata.name, "default")
		}

		CertificateManager cm = new CertificateManager(k8SClient: k8SClient, application: app.metadata.name)
		cm.setupNewCertificate()

		metadataClient = new MetadataClient(EnvironmentConfig.host, EnvironmentConfig.jskStorePath, keystorePass)
		await().atMost(20, SECONDS).until{
			certificateIsReady(metadataClient, app.metadata.name)}
    }



    private Application newTestApp() {
        new Application().with {
            apiVersion = "applicationconnector.kyma-project.io/v1alpha1"
            kind = "Application"
            metadata = new Metadata(name: "test-app-e2e")
            spec = new Spec(description: "Application for testing purpose")
            it
            }
    }

    def certificateIsReady(MetadataClient metadataClient, String appName){
		assert metadataClient!= null

        try{
            def res = metadataClient.getServices(appName)
            
            return res.status == 200
        }
        catch(e){
            return false
        }
    }

    def cleanupSpec() {
        k8SClient.deleteApplication(app.metadata.name)
        printf("Application %s deleted\n", app.metadata.name)
        k8SClient.deleteTokenRequest(app.metadata.name)
        printf("Request token %s deleted\n", app.metadata.name)
        new File(EnvironmentConfig.jskStorePath).delete()
    }

    def "should return empty service list"() {
        when:
            def res = metadataClient.getServices(app.metadata.name)
        then:
        res.status == 200
        ((List)res.getData()).size() == 0
    }

    def ready(String appName){
        try{
            def res = metadataClient.getServices(appName)

            ((List)res.getData()).size() == 1
        }
        catch(e){
            return false
        }
    }

    def "should create service and return service list with one item"() {
        given:
            def service = "{\n" +
                    "\"provider\": \"SAP Hybris\",\n" +
                    "\"name\": \"test-proxy-basic-auth\",\n" +
                    "\"description\": \"httpbin.org\",\n" +
                    "\"api\": {\n" +
                    "  \"targetUrl\": \"https://httpbin.org\",\n" +
                    "  \"credentials\" : {\n" +
                    "    \"basic\" : {\n" +
                    "      \"username\" : \"user\",\n" +
                    "      \"password\" : \"pass\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "}"
        when:
            metadataClient.createService(app.metadata.name, service)
        then:
        await().atMost(10, SECONDS).until{ready(app.metadata.name)}

    }
}
