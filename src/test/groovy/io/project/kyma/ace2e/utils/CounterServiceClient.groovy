package io.project.kyma.ace2e.utils

import groovyx.net.http.RESTClient

class CounterServiceClient {

    RESTClient restClient

    CounterServiceClient(String basicPath) {
        this.restClient = new RESTClient(basicPath)
        restClient.ignoreSSLIssues()
    }

    def getCounter() {
        restClient.get(path: "/counter")
    }

    def checkHealth() {
        restClient.get(path: "/health")
    }
}
