package client

import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CustomObjectsApi
import io.kubernetes.client.models.V1DeleteOptions
import io.kubernetes.client.util.Config
import io.kubernetes.client.util.KubeConfig
import model.Definition
import model.RemoteEnviroment

class K8SClient {

    private CustomObjectsApi api;

    K8SClient(KubeConfig kubeConfig) {
        ApiClient client;
        if(kubeConfig != null) {
            client = Config.fromConfig(kubeConfig)
        } else {
            client = Config.defaultClient();
        }
        Configuration.setDefaultApiClient(client);
        api = new CustomObjectsApi();
    }

    def createRemoteEnviroment(RemoteEnviroment body) {
        return api.createClusterCustomObject(Definition.REMOTE_ENV.getGroup(), Definition.REMOTE_ENV.version, Definition.REMOTE_ENV.plural, body, "true")
    }

    def deleteRemoteEnviroment(String REname) {
        return api.deleteClusterCustomObject(Definition.REMOTE_ENV.getGroup(), Definition.REMOTE_ENV.version, Definition.REMOTE_ENV.plural, REname, new V1DeleteOptions(), 0, null, "Background")
    }
}
