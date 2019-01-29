package io.project.kyma.ace2e.utils

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class KymaClient {

    RESTClient restClient

    KymaClient(String basicPath, String jksStorePath, String keystorePass) {
        final String certURL = "file://localhost${jksStorePath}"
        restClient = new RESTClient(basicPath, ContentType.JSON)
        restClient.auth.certificate(certURL, keystorePass)
    }

    def get(Map<String, ?> args) {
        restClient.get(args)
    }

    def post(Map<String, ?> args) {
        restClient.post(args)
    }
}
