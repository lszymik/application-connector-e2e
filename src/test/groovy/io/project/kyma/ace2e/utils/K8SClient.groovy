package io.project.kyma.ace2e.utils

import com.google.gson.internal.LinkedTreeMap
import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.AppsV1Api
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.apis.CustomObjectsApi
import io.kubernetes.client.models.V1DeleteOptions
import io.kubernetes.client.models.V1Deployment
import io.kubernetes.client.models.V1Service
import io.kubernetes.client.util.Config
import io.project.kyma.ace2e.model.k8s.*

import java.util.stream.Collectors

class K8SClient {

    static final String CONNECTOR_API_GROUP = "applicationconnector.kyma-project.io"
    static final String EVENTING_API_GROUP = "eventing.kyma-project.io"
    static final String SERVICE_CATALOG_API_GROUP = "servicecatalog.k8s.io"
    static final String KUBELESS_API_GROUP = "kubeless.io"
    static final String ISTIO_API_GROUP = "networking.istio.io"
    public static final String V1ALPHA1_API_VERSION = "v1alpha1"
    static final String V1ALPHA3_API_VERSION = "v1alpha3"
    static final String V1BETA1_API_VERSION = "v1beta1"
    static final String APPLICATIONS = "applications"
    static final String TOKEN_REQUESTS = "tokenrequests"
    static final String APPLICATION_MAPPINGS = "applicationmappings"
    static final String SUBSCRIPTIONS = "subscriptions"
    static final String SERVICE_INSTANCES = "serviceinstances"
    static final String SERVICE_CLASSES = "serviceclasses"
    static final String FUNCTIONS = "functions"
    static final String VIRTUAL_SERVICES = "virtualservices"

    static final String TEST_LABEL = "test"
    static final String TEST_LABEL_VALUE = "ac-e2e"

    private CustomObjectsApi customObjApi

    private CoreV1Api coreApi

    private AppsV1Api appsApi

    K8SClient(String kubeConfig) {
        final ApiClient client = (kubeConfig != null && !kubeConfig.empty) ? Config.fromConfig(kubeConfig) : Config.defaultClient()

        Configuration.setDefaultApiClient(client)

        customObjApi = new CustomObjectsApi()
        coreApi = new CoreV1Api()
        appsApi = new AppsV1Api()
    }

    def createApplication(Application app) {
        def namespace = app.metadata.namespace
        customObjApi.createNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATIONS, app, "true")
    }

    def getApplication(String app, String namespace) {
        customObjApi.getNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATIONS, app)
    }

    def deleteTestApplications() {
        LinkedTreeMap response = customObjApi.listClusterCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, APPLICATIONS, "true", "test=ac-e2e", "", false)
        List<Application> apps = (List<Application>) response.get("items")
        apps.each { app ->
            deleteApplicationMapping(app.metadata.name, KymaNames.INTEGRATION_NAMESPACE)
            deleteApplication(app.metadata.name, KymaNames.INTEGRATION_NAMESPACE)
        }
    }

    def deleteApplication(String appName, String namespace) {
        try {
            customObjApi.deleteNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION,
                    namespace,
                    APPLICATIONS,
                    appName,
                    new V1DeleteOptions(),
                    0,
                    null,
                    "Background")
        } catch (Exception e) {
            println "Cannot delete the Application. Reason: " + e.toString()
        }
    }

    def getTokenRequest(String appName, String namespace) {
        customObjApi.getNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, TOKEN_REQUESTS, appName)
    }

    def createTokenRequest(String appName, String namespace) {
        TokenRequest tr = new TokenRequest().with {
            metadata = new Metadata(name: appName)
            apiVersion = "${CONNECTOR_API_GROUP}/${V1ALPHA1_API_VERSION}"
            kind = "TokenRequest"
            it
        }

        customObjApi.createNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, TOKEN_REQUESTS, tr, "true")
    }

    def deleteTokenRequest(String appName, String namespace) {
        try {
            customObjApi.deleteNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace,
                    TOKEN_REQUESTS,
                    appName,
                    new V1DeleteOptions(),
                    0,
                    null,
                    "Background")
        } catch (Exception e) {
            println "Cannot delete the TokenRequest. Reason: " + e.toString()
        }
    }

    def getApplicationMapping(String app, String namespace) {
        customObjApi.getNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATION_MAPPINGS, app)
    }

    def bindApplicationToNamespace(String appName, String namespace, boolean check = false) {
        if (!check || !checkApplicationMappingExists(appName, namespace)) {
            ApplicationMapping am = new ApplicationMapping().with {
                metadata = new Metadata(name: appName, namespace: namespace)
                apiVersion = "${CONNECTOR_API_GROUP}/${V1ALPHA1_API_VERSION}"
                kind = "ApplicationMapping"
                it
            }

            customObjApi.createNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATION_MAPPINGS, am, "true")
        }
    }

    def deleteApplicationMapping(String appName, String namespace) {
        try {
            if (checkApplicationMappingExists(appName, namespace)) {
                customObjApi.deleteNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION,
                        namespace,
                        K8SClient.APPLICATION_MAPPINGS,
                        appName,
                        new V1DeleteOptions(),
                        0,
                        null,
                        "Background")
            }
        } catch (Exception e) {
            println "Cannot delete the ApplicationMapping. Reason: " + e.toString()
        }
    }

    private def checkApplicationMappingExists(String app, String namespace) {
        try {
            getApplicationMapping(app, namespace)
            return true
        }
        catch (final Exception ignore) {
            return false
        }
    }

    def getSubscription(String name, String namespace) {
        customObjApi.getNamespacedCustomObject(EVENTING_API_GROUP, V1ALPHA1_API_VERSION, namespace, SUBSCRIPTIONS, name)
    }

    def createSubscription(Subscription subscription) {
        def namespace = subscription.metadata.namespace
        customObjApi.createNamespacedCustomObject(EVENTING_API_GROUP, V1ALPHA1_API_VERSION, namespace, SUBSCRIPTIONS, subscription, "true")
    }

    def deleteSubscription(String subscriptionName, String namespace) {
        try {
            customObjApi.deleteNamespacedCustomObject(EVENTING_API_GROUP, V1ALPHA1_API_VERSION,
                    namespace,
                    SUBSCRIPTIONS,
                    subscriptionName,
                    new V1DeleteOptions(),
                    0,
                    null,
                    "Background")

        }
        catch (Exception e) {
            println "Cannot delete the Subscription. Reason: " + e.toString()
        }
    }

    def getServiceInstance(String name, String namespace) {
        customObjApi.getNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_INSTANCES, name)
    }

    def createServiceInstance(ServiceInstance serviceInstance) {
        try {
            def namespace = serviceInstance.metadata.namespace
            customObjApi.createNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_INSTANCES, serviceInstance, "true")
        }
        catch (Exception ignore) {
            println "Cannot create the ServiceInstance."
        }
    }

    def deleteServiceInstance(String name, String namespace) {
        try {
            customObjApi.deleteNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_INSTANCES, name, new V1DeleteOptions(),
                    0,
                    null,
                    "Background")
        }
        catch (final Exception e) {
            println "Cannot delete the ServiceInstance. Reason: " + e.toString()
        }
    }

    def getServiceClass(String serviceID, String namespace) {
        customObjApi.getNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_CLASSES, serviceID)
    }

    def createLambdaFunction(LambdaFunction lambda) {
        def namespace = lambda.metadata.namespace
        customObjApi.createNamespacedCustomObject(KUBELESS_API_GROUP, V1BETA1_API_VERSION, namespace, FUNCTIONS, lambda, "true")
    }

    def deleteLambdaFunction(String name, String namespace) {
        try {
            customObjApi.deleteNamespacedCustomObject(KUBELESS_API_GROUP, V1BETA1_API_VERSION,
                    namespace,
                    FUNCTIONS,
                    name,
                    new V1DeleteOptions(),
                    0,
                    null,
                    "Background")
        }
        catch (Exception e) {
            println "Cannot delete the test Lambda. Reason: " + e.toString()
        }
    }

    def getVirtualService(String name, String namespace) {
        customObjApi.getNamespacedCustomObject(ISTIO_API_GROUP, V1ALPHA3_API_VERSION, namespace, VIRTUAL_SERVICES, name)
    }

    def createVirtualService(Object virtualService, String namespace) {
        customObjApi.createNamespacedCustomObject(ISTIO_API_GROUP, V1ALPHA3_API_VERSION, namespace, VIRTUAL_SERVICES, virtualService, "true")
    }

    def deleteVirtualService(String name, String namespace) {
        try {
            customObjApi.deleteNamespacedCustomObject(ISTIO_API_GROUP, V1ALPHA3_API_VERSION,
                    namespace,
                    VIRTUAL_SERVICES,
                    name,
                    new V1DeleteOptions(),
                    0,
                    null,
                    "Background")
        } catch (Exception e) {
            println "Cannot delete the VirtualService. Reason: " + e.toString()
        }
    }

    def getK8SService(String name, String namespace) {
        coreApi.readNamespacedServiceStatus(name, namespace, "true")
    }

    def createK8SService(V1Service service) {
        def namespace = service.metadata.namespace
        coreApi.createNamespacedService(namespace, service, "true")
    }

    void deleteService(String name, String namespace) {
        try {
            coreApi.deleteNamespacedService(name,
                    namespace,
                    new V1DeleteOptions(),
                    "true",
                    0,
                    null,
                    "Background")
        } catch (Exception e) {
            println "Cannot delete the Service. Reason: " + e.toString()
        }
    }

    def createDeployment(V1Deployment deployment) {
        def namespace = deployment.metadata.namespace
        appsApi.createNamespacedDeployment(namespace, deployment, "true")
    }

    def deleteDeployment(String name, String namespace) {
        try {
            appsApi.deleteNamespacedDeployment(name,
                    namespace,
                    new V1DeleteOptions(),
                    "true",
                    0,
                    null,
                    "Background")
        } catch (Exception e) {
            println "Cannot delete the Service. Reason: " + e.toString()
        }
    }

    def getPods(String name, String namespace) {
        coreApi.listNamespacedPod(namespace, "true", null, null, true, null, null, null, 30, false)
                .items.stream().filter({ pod -> pod.metadata.name.contains(name) }).collect(Collectors.toList())
    }
}