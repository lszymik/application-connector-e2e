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
        printf("Application %s created\n", app.metadataName)
        k8SClient.createRequestToken(app.metadataName)
        printf("Request token %s created\n", app.metadataName)

        def token = RetryClosure.retry(
            { k8SClient.getRequestToken(app.metadataName) },
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

        sleep(5000)

        KeyStoreInitializer.createJKSFileWithCert(certFile, keyFile, keystorePass, EnvStore.jskStorePath)
        metadataClient = new MetadataClient(EnvStore.host, EnvStore.jskStorePath, keystorePass)
    }

    def cleanupSpec() {
        k8SClient.deleteApplication(app.metadataName)
        printf("Application %s deleted\n", app.metadataName)
        k8SClient.deleteRequestToken(app.metadataName)
        printf("Request token %s deleted\n", app.metadataName)
        new File(EnvStore.jskStorePath).delete()
    }

    def "Spock check"() {
        given:
            def list = new ArrayList()
        when:
            list.add"Spock"
        then:
            list.size() == 1
    }

    def "should return empty service list"() {
        when:
            def services = metadataClient.getServices(app.metadataName)
        then:
            services.empty
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
            def services = RetryClosure.retry(
                { metadataClient.getServices(app.metadataName) },
                { closureServices -> closureServices.size() == 1 }
            )
        then:
            services.size() == 1
    }
}
