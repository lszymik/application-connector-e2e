package io.project.kyma.ace2e.utils

import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CustomObjectsApi
import io.kubernetes.client.models.V1DeleteOptions
import io.kubernetes.client.util.Config
import io.project.kyma.ace2e.model.Definition
import io.project.kyma.ace2e.model.Application
import io.project.kyma.ace2e.model.TokenRequest

class K8SClient {

    private CustomObjectsApi api

    K8SClient(String kubeConfig) {
        ApiClient client
        if(kubeConfig != null && "null" != kubeConfig && "" != kubeConfig) {
            client = Config.fromConfig(kubeConfig)
        } else {
            client = Config.defaultClient()
        }
        Configuration.setDefaultApiClient(client)
        api = new CustomObjectsApi()
    }

    def createApplication(Application app) {
        return api.createClusterCustomObject(Definition.APP_ENV.getGroup(),
                Definition.APP_ENV.version,
                Definition.APP_ENV.plural,
                app,
                "true")
    }

    def deleteApplication(String appName) {
        return api.deleteClusterCustomObject(Definition.APP_ENV.getGroup(),
                Definition.APP_ENV.version,
                Definition.APP_ENV.plural,
                appName,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }

    def getTokenRequest(String appName) {
        return api.getNamespacedCustomObject(Definition.TOKEN_REQ.getGroup(),
                Definition.TOKEN_REQ.version,
                "default",
                Definition.TOKEN_REQ.plural,
                appName)
    }

    def createTokenRequest(String appName) {
        return api.createNamespacedCustomObject(Definition.TOKEN_REQ.getGroup(),
                Definition.TOKEN_REQ.version,
                "default",
                Definition.TOKEN_REQ.plural,
                new TokenRequest(appName),
                "true")
    }

    def deleteTokenRequest(String appName) {
        return api.deleteNamespacedCustomObject(Definition.TOKEN_REQ.getGroup(),
                Definition.TOKEN_REQ.version,
                "default",
                Definition.TOKEN_REQ.plural,
                appName,
                new V1DeleteOptions(),
                0,
                null,
                "Background")
    }

    def applicationExists(String appName, String namespace) {
        try {
            def obj = api.getNamespacedCustomObject(Definition.APP_ENV.getGroup(), Definition.APP_ENV.version, namespace, Definition.APP_ENV.plural, appName)
            println("Returned object " + obj)
            return obj != null
        }
        catch(e){
            println("Exception: " + e.getMessage())
            return false
        }
    }
}
