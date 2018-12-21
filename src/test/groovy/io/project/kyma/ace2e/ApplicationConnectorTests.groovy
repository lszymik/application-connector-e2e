package io.project.kyma.ace2e

import io.project.kyma.ace2e.model.Application
import io.project.kyma.ace2e.utils.EnvStore
import io.project.kyma.ace2e.utils.K8SClient
import io.project.kyma.ace2e.utils.KeyStoreInitializer
import io.project.kyma.ace2e.utils.MetadataClient
import io.project.kyma.ace2e.utils.TokenExtractor
import io.project.kyma.ace2e.utils.RetryClosure
import io.project.kyma.certificate.KymaConnector
import spock.lang.Shared
import spock.lang.Specification
import static org.awaitility.Awaitility.*
import static java.util.concurrent.TimeUnit.*

class ApplicationConnectorTests extends Specification {

    @Shared MetadataClient metadataClient
    @Shared K8SClient k8SClient
    @Shared Application app

    @Shared def keystorePass = ""

    def setupSpec() {
        println "Starting test"
        EnvStore.readEnv()
        app = Application.buildTestApp()
        k8SClient = new K8SClient(EnvStore.kubeConfig)
        k8SClient.createApplication(app)
        await().atMost(10, SECONDS).until{ k8SClient.applicationExists(app.getMetadataName(), "default") == true }
        
        printf("Application %s created\n", app.metadataName)
        k8SClient.createTokenRequest(app.metadataName)

        printf("Request token %s created\n", app.metadataName)

        def token = RetryClosure.retry(
            { k8SClient.getTokenRequest(app.metadataName) },
            { closureToken -> closureToken.toString().contains("url:") }
        )

        def extractedTokenUrl = TokenExtractor.extract(token.toString())
        println("Extracted tokenURL: " + extractedTokenUrl)

        def kymaConnector = new KymaConnector()
        
        kymaConnector.generateCertificates(extractedTokenUrl, EnvStore.savePath)

        def certFile = new File(EnvStore.certPath)
        def keyFile = new File(EnvStore.keyPath)

        certFile.deleteOnExit()
        keyFile.deleteOnExit()
        
        KeyStoreInitializer.createJKSFileWithCert(certFile, keyFile, keystorePass, EnvStore.jskStorePath)
        metadataClient = new MetadataClient(EnvStore.host, EnvStore.jskStorePath, keystorePass)

        await().atMost(10, SECONDS).until{certificateIsReady(metadataClient, app.getMetadataName())}
    }

    def certificateIsReady(MetadataClient metadataClient, String appName){
        try{
            def res = metadataClient.getServices(appName)
            
            return res.status == 200
        }
        catch(e){
            return false
        }
    }

    def cleanupSpec() {
        k8SClient.deleteApplication(app.metadataName)
        printf("Application %s deleted\n", app.metadataName)
        k8SClient.deleteTokenRequest(app.metadataName)
        printf("Request token %s deleted\n", app.metadataName)
        new File(EnvStore.jskStorePath).delete()
    }

    def "should return empty service list"() {
        when:
            def res = metadataClient.getServices(app.metadataName)
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
            metadataClient.createService(app.metadataName, service)

//            def services = RetryClosure.retry(
//                { metadataClient.getServices(app.metadataName) },
//                { closureServices -> closureServices.size() == 1 }
//            )
        then:
        await().atMost(10, SECONDS).until{ready(app.getMetadataName())}

    }
}
