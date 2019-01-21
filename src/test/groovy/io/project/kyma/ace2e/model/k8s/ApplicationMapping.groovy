package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class ApplicationMapping {
    String apiVersion
    String kind
    Metadata metadata


    @Override
    String toString() {
        return "ApplicationMapping{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                '}'
    }
}
