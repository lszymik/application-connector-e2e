package io.project.kyma.ace2e

import groovy.json.JsonSlurper
import io.kubernetes.client.models.V1Deployment
import io.kubernetes.client.models.V1Service
import io.kubernetes.client.util.Yaml
import io.project.kyma.ace2e.model.k8s.*
import io.project.kyma.ace2e.utils.*
import org.awaitility.core.ConditionTimeoutException
import spock.lang.Shared
import spock.lang.Stepwise

import static org.apache.http.HttpStatus.SC_OK

@Stepwise
class EventLambdaFlowTests extends AbstractKymaTest {

    private static final String COUNTER_SERVICE = "counter-service"

    @Shared
    String testServiceName = "counter-service"

    def setupSpec() {
        deployCounterService()

        checkCounterServiceReadiness()
        ensureApplicationMappedToEnvironment()
    }

    def cleanupSpec() {
        sharedSource.k8SClient.deleteVirtualService(COUNTER_SERVICE, KymaNames.PRODUCTION_NAMESPACE)
        sharedSource.k8SClient.deleteService(COUNTER_SERVICE, KymaNames.PRODUCTION_NAMESPACE)
        sharedSource.k8SClient.deleteDeployment(COUNTER_SERVICE, KymaNames.PRODUCTION_NAMESPACE)
    }

    def "should ping counter service"() {
        when:
        def response = sharedSource.counterClient.checkHealth()

        then:
        response.status == SC_OK
    }

    def "should trigger lambda function"() {
        given:
        def serviceDefinition = new JsonSlurper().parse(new File('src/test/resources/services/test-events.json'))
        def serviceInstance = "test-service-instance-e2e"

        when:
        def result = sharedSource.apiRegistryClient.createService(sharedSource.applicationName, serviceDefinition)

        then:
        result.status == SC_OK

        when:
        String id = result.data.id
        def serviceResp = sharedSource.apiRegistryClient.getService(sharedSource.applicationName, id)

        then:
        serviceResp.status == SC_OK

        when:
        waitUntilServiceClassReady(id)

        and:
        def serviceClass = sharedSource.k8SClient.getServiceClass(id, KymaNames.PRODUCTION_NAMESPACE)

        final String externalName = serviceClass.spec.externalName

        sharedSource.k8SClient.createServiceInstance(newServiceInstance(serviceInstance, externalName))

        then:
        waitUntilServiceInstanceReady(serviceInstance)

        when:
        sharedSource.k8SClient.createLambdaFunction(newLambdaFunction(sharedSource.eventTopic, sharedSource.lambdaFunction))

        and:
        waitUntilLambdaFunctionReady(sharedSource.lambdaFunction)

        and:
        sharedSource.k8SClient.createSubscription(newSubscription(sharedSource.eventTopic, sharedSource.subscription, sharedSource.lambdaFunction))

        and:
        waitUntilSubscriptionReady(sharedSource.subscription)

        and:
        def preEventCounter = checkCounter()

        and:
        sendEvent(sharedSource.eventTopic)

        then:
        checkLambdaTriggered(preEventCounter)
    }

    private static def checkCounterServiceReadiness() {
        Awaitility.awaitUntil({
            sharedSource.counterClient.checkHealth().status == SC_OK
        }, 5, 180)
    }

    private static def ensureApplicationMappedToEnvironment() {
        sharedSource.k8SClient.bindApplicationToNamespace(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE, true)
    }

    private static def waitUntilServiceInstanceReady(String serviceInstance) {
        Awaitility.awaitUntilWithResult({
            final def instance = sharedSource.k8SClient.getServiceInstance(serviceInstance, KymaNames.PRODUCTION_NAMESPACE)
            final def status = instance?.status?.conditions?.status

            status?.stream().allMatch({ s -> s == "True" })
        }, 5, 60)
    }

    private def deployCounterService() {
        def deployment = (V1Deployment) getYamlFile('/deployments/counter-deployment.yaml')
        def service = (V1Service) getYamlFile('/deployments/counter-service.yaml')
        def virtualService = getVirtualServiceYamlFile('/deployments/counter-virtual-service.yaml')

        sharedSource.k8SClient.createDeployment(deployment)
        sharedSource.k8SClient.createK8SService(service)
        sharedSource.k8SClient.createVirtualService(virtualService, KymaNames.PRODUCTION_NAMESPACE)

        Awaitility.awaitUntil({
            sharedSource.k8SClient.getPods(testServiceName, KymaNames.PRODUCTION_NAMESPACE)
                    .stream()
                    .allMatch({
                pod ->
                    pod.status.conditions
                            .stream()
                            .allMatch({ c -> c.status == "True" })
            })
        }, 5, 180)
    }

    private def getYamlFile(String resourcePath) {
        Yaml.load(new File(getClass().getResource(resourcePath).getFile()))
    }

    private def getVirtualServiceYamlFile(String resourcePath) {
        def resourceString = getTextFromFile(resourcePath).replace('{DOMAIN}', EnvironmentConfig.domain)
        def yaml = new org.yaml.snakeyaml.Yaml()
        yaml.load(resourceString)
    }

    private def waitUntilLambdaFunctionReady(String function) {
        println "Waiting for Lambda"
        Awaitility.awaitUntil({
            sharedSource.k8SClient.getPods(function, KymaNames.PRODUCTION_NAMESPACE)
                    .stream()
                    .allMatch({ pod ->
                pod.status.conditions
                        .stream()
                        .allMatch({ c -> c.status == "True" })
            })
        }, 5, 120)
    }

    private def waitUntilServiceClassReady(String serviceId) {
        println "Waiting for Service Class"
        Awaitility.awaitUntil({
            sharedSource.k8SClient.getServiceClass(serviceId, KymaNames.PRODUCTION_NAMESPACE) != null
        }, 5, 180)
    }

    private def waitUntilSubscriptionReady(String subscription) {
        Awaitility.awaitUntil({
            sharedSource.k8SClient.getSubscription(subscription, KymaNames.PRODUCTION_NAMESPACE)
                    .status?.conditions?.status
                    .stream()
                    .allMatch({ s -> s == "True" })
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

    private Subscription newSubscription(String eventTopic, String subscription, String function) {
        new Subscription().with {
            apiVersion = "${K8SClient.EVENTING_API_GROUP}/${K8SClient.V1ALPHA1_API_VERSION}"
            kind = "Subscription"
            metadata = new Metadata(
                    name: subscription,
                    namespace: KymaNames.PRODUCTION_NAMESPACE,
                    labels: [
                            Function: function
                    ]
            )
            spec = new Subscription.Spec(
                    endpoint: "http://${function}.${KymaNames.PRODUCTION_NAMESPACE}:8080/",
                    event_type: eventTopic,
                    event_type_version: "v1",
                    include_subscription_name_header: true,
                    max_inflight: 400,
                    push_request_timeout_ms: 2000,
                    source_id: sharedSource.applicationName
            )
            it
        }
    }

    private ServiceInstance newServiceInstance(String serviceInstance, String externalName) {
        new ServiceInstance().with {
            apiVersion = "${K8SClient.SERVICE_CATALOG_API_GROUP}/${K8SClient.V1BETA1_API_VERSION}"
            kind = "ServiceInstance"
            metadata = new Metadata(name: serviceInstance, namespace: KymaNames.PRODUCTION_NAMESPACE)
            spec = new ServiceInstance.Spec(serviceClassExternalName: externalName)
            it
        }
    }

    private LambdaFunction newLambdaFunction(String eventTopic, String function) {
        final String appURL = "http://${testServiceName}.${KymaNames.PRODUCTION_NAMESPACE}:8090/counter"
        final String dependencies = getTextFromFile('/lambda/package.json')
        final String jsFunction = getTextFromFile('/lambda/function.js')
                .replace("APP_URL", appURL)
        new LambdaFunction().with {
            apiVersion = "${K8SClient.KUBELESS_API_GROUP}/${K8SClient.V1BETA1_API_VERSION}"
            kind = "Function"
            metadata = new Metadata(
                    name: function,
                    namespace: KymaNames.PRODUCTION_NAMESPACE
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
        sharedSource.eventClient.sendEvent(sharedSource.applicationName, [
                "event-type"        : eventTopic,
                "event-type-version": "v1",
                "event-id"          : "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "event-time"        : "2018-10-16T15:00:00Z",
                "data"              : "some data"
        ])
    }

    private static def checkCounter() {
        String counter = sharedSource.counterClient.getCounter().data.counter
        Integer.valueOf(counter)
    }

    private def getTextFromFile(String path) {
        getClass().getResource(path).getText()
    }
}