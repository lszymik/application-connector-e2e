package io.project.kyma.ace2e.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Application {

    String apiVersion
    String kind
    Metadata metadata
    Spec spec
}