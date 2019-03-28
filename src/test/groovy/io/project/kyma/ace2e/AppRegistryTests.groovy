package io.project.kyma.ace2e

import groovy.json.JsonSlurper
import io.project.kyma.ace2e.model.k8s.Application
import io.project.kyma.ace2e.utils.AbstractKymaTest
import io.project.kyma.ace2e.utils.Awaitility
import io.project.kyma.ace2e.utils.KymaNames
import io.project.kyma.ace2e.utils.TestHelper

import static org.apache.http.HttpStatus.SC_OK

class AppRegistryTests extends AbstractKymaTest {


    def setupSpec() {
        def appCR = TestHelper.createApplicationCR(sharedSource.applicationName)
        sharedSource.k8SClient.createApplication(appCR)

        Awaitility.awaitUntilWithResult({
            final Application app = (Application) sharedSource.k8SClient.getApplication(sharedSource.applicationName, KymaNames.INTEGRATION_NAMESPACE)
            app?.status?.installationStatus?.status == KymaNames.STATUS_DEPLOYED
        }, 10, 30)
    }

    def cleanupSpec() {
        cleanUpTestApplication()
    }

    private static Object cleanUpTestApplication() {
        sharedSource.k8SClient.deleteApplication(sharedSource.applicationName, KymaNames.INTEGRATION_NAMESPACE)
    }

    def "should register an API"() {
        given:
        def serviceDefinition = new JsonSlurper().parse(new File('src/test/resources/services/http-bin-swagger.json'))

        when:
        def result = sharedSource.apiRegistryClient.createService(sharedSource.applicationName, serviceDefinition)

        then:
        result.status == SC_OK
        result.data.size() == 1

        when:
        def registeredService = sharedSource.apiRegistryClient.getService(sharedSource.applicationName, result.data.id)

        then:
        registeredService.status == SC_OK
        registeredService.data.api.targetUrl == "https://httpbin.org"
        registeredService.data.api.spec.swagger == "2.0"
    }

    def "should register an API with basic auth"() {
        given:
        def serviceDefinition = new JsonSlurper().parse(new File('src/test/resources/services/http-bin-swagger-basic.json'))

        when:
        def result = sharedSource.apiRegistryClient.createService(sharedSource.applicationName, serviceDefinition)

        then:
        result.status == SC_OK
        result.data.size() == 1

        when:
        def registeredService = sharedSource.apiRegistryClient.getService(sharedSource.applicationName, result.data.id)

        then:
        registeredService.status == SC_OK
        registeredService.data.api.targetUrl == "https://httpbin.org"
        registeredService.data.api.spec.swagger == "2.0"
        registeredService.data.api.credentials.basic.username == "********"
        registeredService.data.api.credentials.basic.password == "********"
    }

    def "should register an API with OAuth"() {
        given:
        def serviceDefinition = new JsonSlurper().parse(new File('src/test/resources/services/http-bin-swagger-oauth.json'))

        when:
        def result = sharedSource.apiRegistryClient.createService(sharedSource.applicationName, serviceDefinition)

        then:
        result.status == SC_OK
        result.data.size() == 1

        when:
        def registeredService = sharedSource.apiRegistryClient.getService(sharedSource.applicationName, result.data.id)

        then:
        registeredService.status == SC_OK
        registeredService.data.api.targetUrl == "https://httpbin.org"
        registeredService.data.api.spec.swagger == "2.0"
        registeredService.data.api.credentials.oauth.url == "https://oauthdebugger.com/"
        registeredService.data.api.credentials.oauth.clientId == "********"
        registeredService.data.api.credentials.oauth.clientSecret == "********"
    }

    def "should register an Event Catalog"() {
        given:
        def serviceDefinition = new JsonSlurper().parse(new File('src/test/resources/services/streetlight-events.json'))

        when:
        def result = sharedSource.apiRegistryClient.createService(sharedSource.applicationName, serviceDefinition)

        then:
        result.status == SC_OK
        result.data.size() == 1

        when:
        def registeredService = sharedSource.apiRegistryClient.getService(sharedSource.applicationName, result.data.id)

        then:
        registeredService.status == SC_OK
        registeredService.data.events.spec.info.title == "Streetlights API"
        registeredService.data.events.spec.info.version == "1.0.0"
    }

    def "should register an API and fetch its specification"() {
        given:
        def serviceDefinition = new JsonSlurper().parse(new File('src/test/resources/services/http-bin-swagger-fetch.json'))

        when:
        def result = sharedSource.apiRegistryClient.createService(sharedSource.applicationName, serviceDefinition)

        then:
        result.status == SC_OK
        result.data.size() == 1

        when:
        def registeredService = sharedSource.apiRegistryClient.getService(sharedSource.applicationName, result.data.id)

        then:
        registeredService.status == SC_OK
        registeredService.data.api.targetUrl == "https://httpbin.org"
        registeredService.data.api.spec.swagger == "2.0"
    }
}


