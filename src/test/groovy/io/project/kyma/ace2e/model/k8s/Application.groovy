package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Application {
    String apiVersion
    String kind
    Metadata metadata
    Spec spec

    @Override
    String toString() {
        return "Application{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}'
    }
}