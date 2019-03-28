package io.project.kyma.ace2e


import io.project.kyma.ace2e.model.k8s.ApplicationMapping
import io.project.kyma.ace2e.utils.*

import static org.apache.http.HttpStatus.SC_OK

class ApplicationsTests extends AbstractKymaTest {

    def "bindApplicationToEnvironment"() {
        when:
        def result = sharedSource.k8SClient.bindApplicationToNamespace(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE)

        then:
        result.metadata.get("name") == sharedSource.applicationName

        when:
        ApplicationMapping mapping = (ApplicationMapping) sharedSource.k8SClient.getApplicationMapping(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE)

        then:
        mapping.metadata.name == sharedSource.applicationName
    }

    def "listAllServices"() {
        when:
        def result = sharedSource.apiRegistryClient.getServices(sharedSource.applicationName)

        then:
        result.status == SC_OK
        result.data.size() == 0
    }
}
