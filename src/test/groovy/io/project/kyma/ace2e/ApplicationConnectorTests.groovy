package io.project.kyma.ace2e

import io.project.kyma.ace2e.model.k8s.Application
import io.project.kyma.ace2e.model.k8s.LambdaFunction
import io.project.kyma.ace2e.model.k8s.Metadata
import io.project.kyma.ace2e.model.k8s.ServiceInstance

import io.project.kyma.ace2e.model.k8s.Subscription

import io.project.kyma.ace2e.utils.CertificateManager
import io.project.kyma.ace2e.utils.EnvironmentConfig
import io.project.kyma.ace2e.utils.EventClient
import io.project.kyma.ace2e.utils.K8SClient
import io.project.kyma.ace2e.utils.APIRegistryClient
import io.project.kyma.ace2e.utils.KymaRESTClient
import spock.lang.Shared
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS
import static org.apache.http.HttpStatus.SC_OK
import static org.awaitility.Awaitility.await

class ApplicationConnectorTests extends Specification {

    @Shared
    APIRegistryClient apiRegistryClient
    @Shared
    EventClient eventClient
    @Shared
    K8SClient k8SClient = new K8SClient(EnvironmentConfig.kubeConfig)

    @Shared
    def keystorePass = ""
    @Shared
    String namespace = "kyma-integration"
    @Shared
    String application = "test-app-e2e"
    @Shared
    String envNamespace = "production"
    @Shared
    String subscription = "test-subscription-e2e"
    @Shared
    String function = "test-lambda-e2e"
    @Shared
    String serviceInstance = "test-service-instance-e2e" + UUID.randomUUID()
    @Shared
    int port = 8080
    @Shared
    String eventTopic = "exampleEvent"

    def setupSpec() {
        setupClientCertificateInKeyStore()
        createApplicationCRD()
        setupRESTClients()
        createApplicationMappingCRD()
    }

    def "should create service"() {
        given:
        def service = [
                provider   : "SAP",
                name       : "test-service",
                description: "e2e-test",
                events: [
                        spec: [
                                asyncapi: "1.0.0"
                        ]
                ]
        ]
        when:
        def postResp = apiRegistryClient.createService(application, service)

        then:
        postResp.status == SC_OK

        when:
        String id = postResp.data.id
        def serviceResp = apiRegistryClient.getService(application, id)

        then:
        serviceResp.status == SC_OK
        serviceResp.data.provider == "SAP"
        serviceResp.data.name == "test-service"
        serviceResp.data.description == "e2e-test"

        when:
        waitUntilServiceClassReady(id)

        and:
        def serviceClass = k8SClient.getServiceClass(id, envNamespace)
        final String externalName = serviceClass.spec.externalName
        
        k8SClient.createServiceInstance(newServiceInstance(externalName), envNamespace)

        and:
        waitUntilServiceInstanceReady()

        and:
        k8SClient.createLambdaFunction(newLambdaFunction(), envNamespace)

        and:
        waitUntilLambdaFunctionReady()

        and:
        k8SClient.createSubscription(newSubscription(), envNamespace)

        and:
        waitUntilSubscriptionReady()

        and:
        def eventResp = sendEvent()

        then:
        eventResp.status == SC_OK
    }

    def cleanupSpec() {
        k8SClient.deleteApplication(application, namespace)
        k8SClient.deleteApplicationMapping(application, envNamespace)
        k8SClient.deleteSubscription(subscription, envNamespace)
        k8SClient.deleteLambdaFunction(function, envNamespace)
    }

    private def setupClientCertificateInKeyStore() {
        new CertificateManager(k8SClient: k8SClient, application: application, namespace: namespace, keyStorePassword: keystorePass)
                .setupCertificateInKeyStore()
    }

    private def setupRESTClients() {
        def kymaRestClient = new KymaRESTClient(EnvironmentConfig.host, EnvironmentConfig.jksStorePath, keystorePass)
        apiRegistryClient = new APIRegistryClient(kymaRestClient)
        eventClient = new EventClient(kymaRestClient)
        await().atMost(30, SECONDS)
                .pollDelay(2, SECONDS)
                .pollInterval(5, SECONDS)
                .until {
            try {
                apiRegistryClient.getServices(application).status == SC_OK
            }
            catch (e) {
                false
            }
        }
    }

    private def createApplicationCRD() {
        k8SClient.createApplication(newTestApp(), namespace)
        await().atMost(20, SECONDS)
                .pollDelay(2, SECONDS)
                .pollInterval(5, SECONDS)
                .until {
            k8SClient.getApplication(application, namespace) != null
        }
    }

    private def createApplicationMappingCRD() {
        k8SClient.createApplicationMapping(application, envNamespace)
        await().atMost(20, SECONDS)
                .pollDelay(2, SECONDS)
                .pollInterval(5, SECONDS)
                .until {
            k8SClient.getApplicationMapping(application, envNamespace) != null
        }
    }

    private def waitUntilServiceInstanceReady() {
        await().atMost(30, SECONDS)
                .pollDelay(2, SECONDS)
                .pollInterval(5, SECONDS)
                .until {
            try {
                k8SClient.getServiceInstance(serviceInstance, envNamespace) != null
            }
            catch (e) {
                false
            }
        }
    }

    private def waitUntilLambdaFunctionReady() {
        await().atMost(30, SECONDS)
                .pollDelay(2, SECONDS)
                .pollInterval(5, SECONDS)
                .until {
            try {
                k8SClient.getLambdaFunction(function, envNamespace) != null
            }
            catch (e) {
                false
            }
        }
    }

    private def waitUntilServiceClassReady(String serviceId) {
        await().atMost(360, SECONDS)
                .pollDelay(5, SECONDS)
                .pollInterval(30, SECONDS)
                .until {
            try {
                k8SClient.getServiceClass(serviceId, envNamespace) != null
            }
            catch (e) {
                false
            }
        }
    }

    private def waitUntilSubscriptionReady() {
        await().atMost(30, SECONDS)
                .pollDelay(2, SECONDS)
                .pollInterval(5, SECONDS)
                .until {
            try {
                k8SClient.getSubscription(subscription, envNamespace) != null
            }
            catch (e) {
                false
            }
        }
    }

    private Application newTestApp() {
        new Application().with {
            apiVersion = "${K8SClient.CONNECTOR_API_GROUP}/${K8SClient.V1ALPHA1_API_VERSION}"
            kind = "Application"
            metadata = new Metadata(name: application)
            spec = new Application.Spec(description: "Application for testing purpose")
            it
        }
    }

    private Subscription newSubscription() {
        new Subscription().with {
            apiVersion = "${K8SClient.EVENTING_API_GROUP}/${K8SClient.V1ALPHA1_API_VERSION}"
            kind = "Subscription"
            metadata = new Metadata(
                    name: subscription,
                    namespace: envNamespace,
                    labels: [
                            Function: function
                    ]
            )
            spec = new Subscription.Spec(
                    endpoint: "http://${function}.${envNamespace}:${port}/",
                    event_type: eventTopic,
                    event_type_version: "v1",
                    include_subscription_name_header: true,
                    max_inflight: 400,
                    push_request_timeout_ms: 2000,
                    source_id: application
            )
            it
        }
    }

    private ServiceInstance newServiceInstance(String externalName) {
        new ServiceInstance().with {
            apiVersion = "${K8SClient.SERVICE_CATALOG_API_GROUP}/${K8SClient.V1BETA1_API_VERSION}"
            kind = "ServiceInstance"
            metadata = new Metadata(name: serviceInstance, namespace: envNamespace)
            spec = new ServiceInstance.Spec(serviceClassExternalName: externalName)
            it
        }
    }
    
    private LambdaFunction newLambdaFunction() {
        final String dependencies = getClass().getResource('/lambda/package.json').getText()
        final String jsFunction = getClass().getResource('/lambda/function.js').getText()

        new LambdaFunction().with {
            apiVersion = "${K8SClient.KUBELESS_API_GROUP}/${K8SClient.V1BETA1_API_VERSION}"
            kind = "Function"
            metadata = new Metadata(
                    name: function,
                    namespace: envNamespace
            )
            spec = new LambdaFunction.Spec(
                    deps: dependencies,
                    function: jsFunction,
                    functionContentType: "text",
                    handler: "handler.main",
                    runtime: "nodejs8",
                    timeout: "",
                    topic: eventTopic
            )
            it
        }
    }

    def sendEvent() {
        eventClient.sendEvent(application, [
                "event-type": eventTopic,
                "event-type-version": "v1",
                "event-id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "event-time": "2018-10-16T15:00:00Z",
                "data": "some data"
        ])
    }
}
