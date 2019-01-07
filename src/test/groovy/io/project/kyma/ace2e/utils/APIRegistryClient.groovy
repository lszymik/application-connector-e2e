package io.project.kyma.ace2e.utils

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class APIRegistryClient {

	private RESTClient restClient

	APIRegistryClient(String basicMetadataPath, String jksStorePath, String keystorePass) {
		final String certURL = "file://localhost${jksStorePath}"
		restClient = new RESTClient(basicMetadataPath, ContentType.JSON)
		restClient.auth.certificate(certURL, keystorePass)
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
