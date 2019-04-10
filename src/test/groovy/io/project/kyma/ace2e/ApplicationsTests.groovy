package io.project.kyma.ace2e


import io.project.kyma.ace2e.model.k8s.ApplicationMapping
import io.project.kyma.ace2e.utils.*
import spock.lang.Stepwise

import static org.apache.http.HttpStatus.SC_OK

@Stepwise
class ApplicationsTests extends AbstractKymaTest {

    def "bindApplicationToEnvironment"() {
        when:
        sharedSource.k8SClient.bindApplicationToNamespace(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE, true)

        and:
        ApplicationMapping mapping = (ApplicationMapping) sharedSource.k8SClient.getApplicationMapping(sharedSource.applicationName, KymaNames.PRODUCTION_NAMESPACE)

        then:
        mapping.metadata.name == sharedSource.applicationName
    }

    def "listAllServices"() {
        when:
        def result = sharedSource.apiRegistryClient.getServices(sharedSource.applicationName)

        then:
        result.status == SC_OK
    }
}
