package io.project.kyma.ace2e.utils

class EventServiceClient {

    private RestClientWithClientCert restClient

	EventServiceClient(RestClientWithClientCert kymaRESTClient) {
        restClient = kymaRESTClient
    }

    def sendEvent(String appName, Object body) {
        restClient.post(path: "/${appName}/v1/events", body: body)
    }

}
