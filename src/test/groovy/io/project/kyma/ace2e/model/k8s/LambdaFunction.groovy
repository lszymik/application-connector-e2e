package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class LambdaFunction {
    String apiVersion
    Metadata metadata
    Spec spec
    String kind

    @Override
    String toString() {
        return "LambdaFunction{" +
                "apiVersion='" + apiVersion + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                ", kind='" + kind + '\'' +
                '}'
    }


    static class Spec {
        String deps
        String function
        String functionContentType
        String handler
        String runtime
        String timeout
        String topic
    }
}
