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

    def createApplication(Application body) {
        return api.createClusterCustomObject(Definition.APP_ENV.getGroup(), Definition.APP_ENV.version, Definition.APP_ENV.plural, body, "true")
    }

    def deleteApplication(String appName) {
        return api.deleteClusterCustomObject(Definition.APP_ENV.getGroup(), Definition.APP_ENV.version, Definition.APP_ENV.plural, appName, new V1DeleteOptions(), 0, null, "Background")
    }

    def getRequestToken(String appName) {
        return api.getNamespacedCustomObject(Definition.TOKEN_REQ.getGroup(), Definition.TOKEN_REQ.version, "default", Definition.TOKEN_REQ.plural, appName)
    }

    def createRequestToken(String appName) {
        def body = new TokenRequest(appName)
        return api.createNamespacedCustomObject(Definition.TOKEN_REQ.getGroup(), Definition.TOKEN_REQ.version, "default", Definition.TOKEN_REQ.plural, body, "true")
    }

    def deleteRequestToken(String appName) {
        return api.deleteNamespacedCustomObject(Definition.TOKEN_REQ.getGroup(), Definition.TOKEN_REQ.version, "default", Definition.TOKEN_REQ.plural, appName, new V1DeleteOptions(), 0, null, "Background")
    }
}
