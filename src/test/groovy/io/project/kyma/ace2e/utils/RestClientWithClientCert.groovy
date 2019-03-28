package io.project.kyma.ace2e.utils

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

/**
 * Rest client which is invoking calls with client certificate.
 *
 */
class RestClientWithClientCert {

    RESTClient restClient

    /**
     * Construct instance of rest client with client certificate from JSK store.
     *
     * @param defaulUri for all calls.
     * @param jksStorePath store with the client certificate.
     * @param keystorePass password to the security store.
     */
    RestClientWithClientCert(String defaulUri, String jksStorePath, String keystorePass) {
        final String certURL = "file://localhost${jksStorePath}"
        restClient = new RESTClient(defaulUri, ContentType.JSON)
        restClient.auth.certificate(certURL, keystorePass)
    }

    /**
     * Execute the get call.
     *
     * @param args passed to the call.
     * @return result from the execution.
     */
    def get(Map<String, ?> args) {
        restClient.get(args)
    }

    /**
     * Execture the post call.
     *
     * @param args passed to the post call.
     *
     * @return result from the execution.
     */
    def post(Map<String, ?> args) {
        restClient.post(args)
    }
}
