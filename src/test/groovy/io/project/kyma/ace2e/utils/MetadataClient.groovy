package io.project.kyma.ace2e.utils

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

class MetadataClient {

    private RESTClient restClient

    MetadataClient(String basicMetadataPath, String jksStorePath, String keystorePass) {
        def certURL = "file://localhost" + jksStorePath
        restClient = new RESTClient(basicMetadataPath, ContentType.JSON)
        restClient.auth.certificate(certURL, keystorePass)
    }

    def getServices(String appName) {
        def path = String.format("/%s/v1/metadata/services", appName)
        def response = restClient.get(path: path)

        return (HttpResponseDecorator)response
    }

    def getService(String appName, String serviceId) {
        def path = String.format("/%s/v1/metadata/services/%s", appName, serviceId)
        def response = (HttpResponseDecorator) restClient.get(path: path)
        assert response.status == 200
        response.getData()
    }

    def createService(String appName, String body) {
        def path = String.format("/%s/v1/metadata/services", appName)
        def response = (HttpResponseDecorator) restClient.post(path: path, body: body)
        assert response.status == 200
        response.getData()
    }

    def updateService(String appName, String body, String serviceId) {
        def path = String.format("/%s/v1/metadata/services/%s", appName, serviceId)
        def response = (HttpResponseDecorator) restClient.post(path: path, body: body)
        assert response.status == 200
        response.getData()
    }

    def deleteService(String appName, String serviceId) {
        def path = String.format("/%s/v1/metadata/services/%s", appName, serviceId)
        def response = (HttpResponseDecorator) restClient.delete(path: path)
        assert response.status == 200
        response.getData()
    }
}
