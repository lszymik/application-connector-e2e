package io.project.kyma.ace2e.utils

class APIRegistryClient {

    private KymaClient restClient

    APIRegistryClient(KymaClient kymaRESTClient) {
		restClient = kymaRESTClient
	}

	def getServices(String appName) {
		restClient.get(path: "/${appName}/v1/metadata/services")
	}

	def getService(String appName, String serviceId) {
		restClient.get(path: "/${appName}/v1/metadata/services/${serviceId}")
	}

	def createService(String appName, Object body) {
		restClient.post(path: "/${appName}/v1/metadata/services", body: body)
	}
}
