package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Application {
    String apiVersion
    String kind
    Metadata metadata
    Spec spec
    Status status

    @Override
    String toString() {
        return "Application{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                ", status=" + status +
                '}'
    }


    static class Spec {
        String accessLabel
        String description
        Map<String, String> labels
        Object services


        @Override
        String toString() {
            return "Spec{" +
                    "accessLabel='" + accessLabel + '\'' +
                    ", description='" + description + '\'' +
                    ", labels=" + labels +
                    ", services=" + services +
                    '}'
        }
    }

    static class Status {
        InstallationStatus installationStatus

        @Override
        String toString() {
            return "Status{" +
                    "installationStatus=" + installationStatus +
                    '}'
        }
    }

    static class InstallationStatus {
        String description
        String status


        @Override
        String toString() {
            return "InstallationStatus{" +
                    "description='" + description + '\'' +
                    ", status='" + status + '\'' +
                    '}'
        }
    }
}