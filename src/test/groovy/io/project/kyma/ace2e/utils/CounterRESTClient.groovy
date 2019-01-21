package io.project.kyma.ace2e.utils

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class CounterRESTClient {

    RESTClient restClient

    CounterRESTClient(String basicPath) {
        this.restClient = new RESTClient(basicPath, ContentType.JSON)
    }

    def getCounter() {
        restClient.get(path: "/counter")
    }

}
