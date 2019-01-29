package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class ServiceInstance {
    String apiVersion
    String kind
    Metadata metadata
    Spec spec


    @Override
    String toString() {
        return "ServiceInstance{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}'
    }

    static class Spec {
        String serviceClassExternalName


        @Override
        String toString() {
            return "Spec{" +
                    "serviceClassExternalName='" + serviceClassExternalName + '\'' +
                    '}'
        }
    }
}
