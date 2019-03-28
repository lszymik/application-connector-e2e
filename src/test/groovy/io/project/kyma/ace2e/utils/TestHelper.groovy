package io.project.kyma.ace2e.utils

import io.project.kyma.ace2e.model.k8s.Application
import io.project.kyma.ace2e.model.k8s.Metadata

/**
 * Set of helper methods which common parts used in multiple test classes.
 */
class TestHelper {

    /**
     * Creates an application CR with only name.
     *
     * @param appName name of application.
     *
     * @return created application data.
     */
    static Application createApplicationCR(String appName) {
        new Application().with {
            apiVersion = "${K8SClient.CONNECTOR_API_GROUP}/${K8SClient.V1ALPHA1_API_VERSION}"
            kind = "Application"
            metadata = new Metadata(name: appName, namespace: KymaNames.INTEGRATION_NAMESPACE)
            it
        }
    }

    /**
     * Creates application CR with name, description and labels.
     *
     * @param appName name of application.
     * @param description of application.
     * @param labels added to application.
     *
     * @return created application data.
     */
    static Application createApplicationCR(String appName, String description, Map<String, String> labels) {
        Application app = createApplicationCR(appName)
        app.spec = new Application.Spec()
        app.spec.description = description
        app.spec.labels = labels
        app
    }
}
