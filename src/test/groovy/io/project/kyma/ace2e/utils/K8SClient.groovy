package io.project.kyma.ace2e.utils

import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CustomObjectsApi
import io.kubernetes.client.models.V1DeleteOptions
import io.kubernetes.client.util.Config
import io.project.kyma.ace2e.model.k8s.*

class K8SClient {

    static final String CONNECTOR_API_GROUP = "applicationconnector.kyma-project.io"
    static final String EVENTING_API_GROUP = "eventing.kyma-project.io"
    static final String SERVICE_CATALOG_API_GROUP = "servicecatalog.k8s.io"
    static final String KUBELESS_API_GROUP = "kubeless.io"
    static final String V1ALPHA1_API_VERSION = "v1alpha1"
    static final String V1BETA1_API_VERSION = "v1beta1"
    static final String APPLICATIONS = "applications"
    static final String TOKEN_REQUESTS = "tokenrequests"
    static final String APPLICATION_MAPPINGS = "applicationmappings"
    static final String SUBSCRIPTIONS = "subscriptions"
    static final String SERVICE_INSTANCES = "serviceinstances"
    static final String SERVICE_CLASSES = "serviceclasses"
    static final String FUNCTIONS = "functions"
    static final String EVENT_ACTIVATIONS = "eventactivations"

    private CustomObjectsApi customObjApi

    K8SClient(String kubeConfig) {
        final ApiClient client = (kubeConfig != null && !kubeConfig.empty) ? Config.fromConfig(kubeConfig) : Config.defaultClient()

        Configuration.setDefaultApiClient(client)
        customObjApi = new CustomObjectsApi()
    }

    def createApplication(Application app, String namespace) {
        customObjApi.createNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATIONS, app, "true")
    }

    def getApplication(String app, String namespace) {
        customObjApi.getNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATIONS, app)
    }

    def deleteApplication(String appName, String namespace) {
        customObjApi.deleteNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION,
                namespace,
                APPLICATIONS,
                appName,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }

    def getTokenRequest(String appName, String namespace) {
        customObjApi.getNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, TOKEN_REQUESTS, appName)
    }

    def createTokenRequest(String appName, String namespace) {
        TokenRequest tr = new TokenRequest().with {
            metadata = new Metadata(name: appName)
            apiVersion = "${CONNECTOR_API_GROUP}/${v1ALPHA1_API_VERSION}"
            kind = "TokenRequest"
            it
        }

        customObjApi.createNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, TOKEN_REQUESTS, tr, "true")
    }

    def deleteTokenRequest(String appName, String namespace) {
        customObjApi.deleteNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace,
                TOKEN_REQUESTS,
                appName,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }

    def getApplicationMapping(String app, String namespace) {
        customObjApi.getNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATION_MAPPINGS, app)
    }

    def createApplicationMapping(String appName, String namespace) {
        ApplicationMapping am = new ApplicationMapping().with {
            metadata = new Metadata(name: appName, namespace: namespace)
            apiVersion = "${CONNECTOR_API_GROUP}/${v1ALPHA1_API_VERSION}"
            kind = "ApplicationMapping"
            it
        }

        customObjApi.createNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION, namespace, APPLICATION_MAPPINGS, am, "true")
    }

    def deleteApplicationMapping(String appName, String namespace) {
        customObjApi.deleteNamespacedCustomObject(CONNECTOR_API_GROUP, V1ALPHA1_API_VERSION,
                namespace,
                APPLICATION_MAPPINGS,
                appName,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }

    def getSubscription(String name, String namespace) {
        customObjApi.getNamespacedCustomObject(EVENTING_API_GROUP, V1ALPHA1_API_VERSION, namespace, SUBSCRIPTIONS, name)
    }

    def createSubscription(Subscription subscription, String namespace) {
        customObjApi.createNamespacedCustomObject(EVENTING_API_GROUP, V1ALPHA1_API_VERSION, namespace, SUBSCRIPTIONS, subscription, "true")
    }

    def deleteSubscription(String subscriptionName, String namespace) {
        customObjApi.deleteNamespacedCustomObject(EVENTING_API_GROUP, V1ALPHA1_API_VERSION,
                namespace,
                SUBSCRIPTIONS,
                subscriptionName,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }

    def getServiceInstance(String name, String namespace) {
        customObjApi.getNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_INSTANCES, name)
    }

    def createServiceInstance(ServiceInstance serviceInstance, String namespace) {
        customObjApi.createNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_INSTANCES, serviceInstance, "true")
    }

    def getServiceClass(String serviceID, String namespace) {
        customObjApi.getNamespacedCustomObject(SERVICE_CATALOG_API_GROUP, V1BETA1_API_VERSION, namespace, SERVICE_CLASSES, serviceID)
    }

    def getLambdaFunction(String name, String namespace) {
        customObjApi.getNamespacedCustomObject(KUBELESS_API_GROUP, V1BETA1_API_VERSION, namespace, FUNCTIONS, name)
    }

    def createLambdaFunction(LambdaFunction lambda, String namespace) {
        customObjApi.createNamespacedCustomObject(KUBELESS_API_GROUP, V1BETA1_API_VERSION, namespace, FUNCTIONS, lambda, "true")
    }

    def deleteLambdaFunction(String name, String namespace) {
        customObjApi.deleteNamespacedCustomObject(KUBELESS_API_GROUP, V1BETA1_API_VERSION,
                namespace,
                FUNCTIONS,
                name,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }
}
