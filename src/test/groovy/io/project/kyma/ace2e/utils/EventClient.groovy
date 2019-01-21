package io.project.kyma.ace2e.utils

class EventClient {

    private KymaRESTClient restClient

    EventClient(KymaRESTClient kymaRESTClient) {
        restClient = kymaRESTClient
    }

    def sendEvent(String appName, Object body) {
        restClient.post(path: "/${appName}/v1/events", body: body)
    }

}
