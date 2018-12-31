package io.project.kyma.ace2e.utils

import io.kubernetes.client.ApiClient
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CustomObjectsApi
import io.kubernetes.client.models.V1DeleteOptions
import io.kubernetes.client.util.Config
import io.project.kyma.ace2e.model.k8s.Application
import io.project.kyma.ace2e.model.k8s.Metadata
import io.project.kyma.ace2e.model.k8s.TokenRequest

class K8SClient {

	static final String API_GROUP = "applicationconnector.kyma-project.io"
	static final String API_VERSION = "v1alpha1"
	static final String APPLICATIONS = "applications"
	static final String TOKEN_REQUESTS = "tokenrequests"

	K8SClient(String kubeConfig) {
		final ApiClient client = (kubeConfig != null)? Config.fromConfig(kubeConfig) : Config.defaultClient()

		Configuration.setDefaultApiClient(client)
		api = new CustomObjectsApi()
	}

	def createApplication(Application app, String namespace) {
		api.createNamespacedCustomObject(API_GROUP, API_VERSION, namespace, APPLICATIONS, app, "true")
	}

	def getApplication(String app, String namespace) {
		api.getNamespacedCustomObject(API_GROUP, API_VERSION, namespace, APPLICATIONS, app)
	}

	def deleteApplication(String appName, String namespace) {
		api.deleteNamespacedCustomObject(API_GROUP, API_VERSION,
				namespace,
				APPLICATIONS,
				appName,
				new V1DeleteOptions(),
				0,
				null,
				"Background")
	}

	def getTokenRequest(String appName, String namespace) {
		api.getNamespacedCustomObject(API_GROUP, API_VERSION, namespace, TOKEN_REQUESTS, appName)
	}

	def createTokenRequest(String appName, String namespace) {
		TokenRequest tr = new TokenRequest().with {
			metadata = new Metadata(name: appName)
			apiVersion = "${API_GROUP}/${API_VERSION}"
			kind = "TokenRequest"
			it
		}

		api.createNamespacedCustomObject(API_GROUP, API_VERSION, namespace, TOKEN_REQUESTS, tr, "true")
	}

	def deleteTokenRequest(String appName, String namespace) {
		api.deleteNamespacedCustomObject(API_GROUP, API_VERSION, namespace,
				TOKEN_REQUESTS,
				appName,
				new V1DeleteOptions(),
				0,
				null,
				"Background")
	}

	private CustomObjectsApi api
}
