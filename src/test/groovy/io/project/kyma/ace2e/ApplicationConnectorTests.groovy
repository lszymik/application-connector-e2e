package io.project.kyma.ace2e

import io.kubernetes.client.models.V1Deployment
import io.kubernetes.client.models.V1Service
import io.kubernetes.client.util.Yaml
import io.project.kyma.ace2e.model.k8s.*
import io.project.kyma.ace2e.utils.*
import org.awaitility.core.ConditionTimeoutException
import org.yaml.snakeyaml.Yaml as SnakeYaml
import spock.lang.Shared
import spock.lang.Specification

import static org.apache.http.HttpStatus.SC_OK

class ApplicationConnectorTests extends Specification {

    @Shared
    APIRegistryClient apiRegistryClient

    @Shared
    EventServiceClient eventClient

    @Shared
    CounterServiceClient counterClient

    @Shared
    K8SClient k8SClient = new K8SClient(EnvironmentConfig.kubeConfig)

    @Shared
    def keystorePass = ""

    @Shared
    String integrationNamespace = "kyma-integration"

    @Shared
    String application = "test-app-e2e"

    @Shared
    String productionNamespace = "production"

    @Shared
    String testServiceName = "counter-service"

    @Shared
    String resourceConditionStatusTrue = "True"

    def setupSpec() {
        setupClientCertificateInKeyStore()
        createApplicationCRD()
        setupKymaClients()
        createApplicationMappingCRD()
        deployCounterService()
        setupCounterServiceClient()
    }

    def cleanupSpec() {
        k8SClient.deleteApplication(application, integrationNamespace)
        k8SClient.deleteApplicationMapping(application, productionNamespace)
        k8SClient.deleteDeployment(testServiceName, productionNamespace)
        k8SClient.deleteService(testServiceName, productionNamespace)
        k8SClient.deleteVirtualService(testServiceName, productionNamespace)
    }

    def "should trigger lambda function"() {
        given:
        def service = [
                provider   : "SAP",
                name       : "test-service",
                description: "e2e-test",
                events     : [
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

        when:
        waitUntilServiceClassReady(id)

        and:
        def serviceClass = k8SClient.getServiceClass(id, productionNamespace)
        final String serviceInstance = "test-service-instance-e2e"
        final String externalName = serviceClass.spec.externalName

        k8SClient.createServiceInstance(newServiceInstance(serviceInstance, externalName))

        and:
        waitUntilServiceInstanceReady(serviceInstance)

        and:
        final String eventTopic = "exampleEvent"
        final String lambdaFunction = "test-lambda-e2e"

        k8SClient.createLambdaFunction(newLambdaFunction(eventTopic, lambdaFunction))

        and:
        waitUntilLambdaFunctionReady(lambdaFunction)

        and:
        final String subscription = "test-subscription-e2e"

        k8SClient.createSubscription(newSubscription(eventTopic, subscription, lambdaFunction))

        and:
        waitUntilSubscriptionReady(subscription)

        and:
        def preEventCounter = checkCounter()

        and:
        sendEvent(eventTopic)

        then:
        checkLambdaTriggered(preEventCounter)

        cleanup:
        k8SClient.deleteSubscription(subscription, productionNamespace)
        k8SClient.deleteLambdaFunction(lambdaFunction, productionNamespace)
    }



    private def setupClientCertificateInKeyStore() {
        new CertificateManager(k8SClient: k8SClient, application: application, namespace: integrationNamespace, keyStorePassword: keystorePass)
                .setupCertificateInKeyStore()
    }

    private def setupCounterServiceClient() {
        println "Setup Counter Service Client"
        def basePath = "https://counter-service.${EnvironmentConfig.domain}"
        counterClient = new CounterServiceClient(basePath)
        Awaitility.awaitUntil({
            counterClient.checkHealth().status == SC_OK
        }, 5, 180)
    }

    private def setupKymaClients() {
        println "Setup Kyma Clients"
        final String nodePort = EnvironmentConfig.nodePort
        final String domain = EnvironmentConfig.domain
        final String basePath = nodePort != null? "https://gateway.${domain}:${nodePort}" : "https://gateway.${domain}"

        def kymaRestClient = new RestClientWithClientCert(basePath, EnvironmentConfig.jksStorePath, keystorePass)
        apiRegistryClient = new APIRegistryClient(kymaRestClient)
        eventClient = new EventServiceClient(kymaRestClient)

        Awaitility.awaitUntil({
            apiRegistryClient.getServices(application).status == SC_OK
        }, 5, 180)
    }

    private def createApplicationCRD() {
        println "Waiting for Application"
        k8SClient.createApplication(newTestApp())
        Awaitility.awaitUntil({
            final Application app = (Application) k8SClient.getApplication(application, integrationNamespace)

            app?.status?.installationStatus?.status == "DEPLOYED"
        }, 5, 180)
    }

    private def createApplicationMappingCRD() {
        k8SClient.bindApplicationToNamespace(application, productionNamespace)
    }

    private void deployCounterService() {
        println "Deploying Counter Service"
        createDeployment()
        createService()
        createVirtualService()
    }

    private def createDeployment() {
        def deployment = (V1Deployment) getk8sResourceFromYaml('/yaml/deployment.yaml')
        k8SClient.createDeployment(deployment)
        Awaitility.awaitUntil({
            k8SClient.getPods(testServiceName, productionNamespace)
                    .stream()
                    .allMatch({
                pod ->
                    pod.status.conditions
                            .stream()
                            .allMatch({c -> c.status == resourceConditionStatusTrue})
            })
        }, 5, 180)
    }

    private def createService() {
        def service = (V1Service) getk8sResourceFromYaml('/yaml/service.yaml')
        k8SClient.createK8SService(service)
    }

    private def getk8sResourceFromYaml(String resourcePath) {
        Yaml.load(new File(getClass().getResource(resourcePath).getFile()))
    }

    private def createVirtualService() {
        def virtualService = getVirtualServiceFromYaml('/yaml/vs.yaml')
        k8SClient.createVirtualService(virtualService, productionNamespace)
    }

    private def getVirtualServiceFromYaml(String resourcePath) {
        def resourceString = getTextFromFile(resourcePath).replace('{DOMAIN}', EnvironmentConfig.domain)
        def yaml = new SnakeYaml()
        yaml.load(resourceString)
    }

    private def waitUntilServiceInstanceReady(String serviceInstance) {
        println "Waiting for Service Instance"
        Awaitility.awaitUntil({
            final def instance = k8SClient.getServiceInstance(serviceInstance, productionNamespace)
            final def status = instance?.status?.conditions?.status

            status?.stream().allMatch({ s -> s == resourceConditionStatusTrue })
        }, 5, 60)
    }

    private def waitUntilLambdaFunctionReady(String function) {
        println "Waiting for Lambda"
        Awaitility.awaitUntil({
            k8SClient.getPods(function, productionNamespace)
                    .stream()
                    .allMatch({ pod ->
                pod.status.conditions
                        .stream()
                        .allMatch({c -> c.status == resourceConditionStatusTrue })
            })
        }, 5, 120)
    }

    private def waitUntilServiceClassReady(String serviceId) {
        println "Waiting for Service Class"
        Awaitility.awaitUntil({
            k8SClient.getServiceClass(serviceId, productionNamespace) != null
        }, 5, 360)
    }

    private def waitUntilSubscriptionReady(String subscription) {
        Awaitility.awaitUntil({
            k8SClient.getSubscription(subscription, productionNamespace)
                    .status?.conditions?.status
                    .stream()
                    .allMatch({ s -> s == resourceConditionStatusTrue })
        }, 5, 30)
    }

    private def checkLambdaTriggered(int preEventCounter) {
        println "Checking if Lambda was triggered"
        try {
            Awaitility.awaitUntil({
                def postEventCounter = checkCounter()
                postEventCounter == preEventCounter + 1
            }, 30, 120)

            true
        } catch (ConditionTimeoutException e) {
            false
        }
    }

    private Application newTestApp() {
        new Application().with {
            apiVersion = "${K8SClient.CONNECTOR_API_GROUP}/${K8SClient.V1ALPHA1_API_VERSION}"
            kind = "Application"
            metadata = new Metadata(name: application, namespace: integrationNamespace)
            spec = new Application.Spec(description: "Application for testing purpose")
            it
        }
    }

    private Subscription newSubscription(String eventTopic, String subscription, String function) {
        new Subscription().with {
            apiVersion = "${K8SClient.EVENTING_API_GROUP}/${K8SClient.V1ALPHA1_API_VERSION}"
            kind = "Subscription"
            metadata = new Metadata(
                    name: subscription,
                    namespace: productionNamespace,
                    labels: [
                            Function: function
                    ]
            )
            spec = new Subscription.Spec(
                    endpoint: "http://${function}.${productionNamespace}:8080/",
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

    private ServiceInstance newServiceInstance(String serviceInstance, String externalName) {
        new ServiceInstance().with {
            apiVersion = "${K8SClient.SERVICE_CATALOG_API_GROUP}/${K8SClient.V1BETA1_API_VERSION}"
            kind = "ServiceInstance"
            metadata = new Metadata(name: serviceInstance, namespace: productionNamespace)
            spec = new ServiceInstance.Spec(serviceClassExternalName: externalName)
            it
        }
    }

    private LambdaFunction newLambdaFunction(String eventTopic, String function) {
        final String appURL = "http://${testServiceName}.${productionNamespace}:8090/counter"
        final String dependencies = getTextFromFile('/lambda/package.json')
        final String jsFunction = getTextFromFile('/lambda/function.js')
                .replace("APP_URL", appURL)
        new LambdaFunction().with {
            apiVersion = "${K8SClient.KUBELESS_API_GROUP}/${K8SClient.V1BETA1_API_VERSION}"
            kind = "Function"
            metadata = new Metadata(
                    name: function,
                    namespace: productionNamespace
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

    private sendEvent(String eventTopic) {
        println "Sending Event"
        eventClient.sendEvent(application, [
                "event-type"        : eventTopic,
                "event-type-version": "v1",
                "event-id"          : "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "event-time"        : "2018-10-16T15:00:00Z",
                "data"              : "some data"
        ])
    }

    private def checkCounter() {
        String counter = counterClient.getCounter().data.counter
        Integer.valueOf(counter)
    }

    private def getTextFromFile(String path) {
        getClass().getResource(path).getText()
    }

}
