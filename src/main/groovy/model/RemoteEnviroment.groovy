package model;

class RemoteEnviroment {

    private String apiVersion
    private String kind
    private Metadata metadata
    private Spec spec

    private static createMetadata(RemoteEnviroment remoteEnviroment) {
        return new Metadata(remoteEnviroment)
    }

    private static createSpec(RemoteEnviroment remoteEnviroment) {
        return new Spec(remoteEnviroment)
    }

    private static createSpecLabels(RemoteEnviroment remoteEnviroment) {
        return new Spec.Labels(remoteEnviroment.spec)
    }

    static RemoteEnviroment buildTestRE() {
        def builder = new REBuilder()
        builder.withApiVersion("applicationconnector.kyma-project.io/v1alpha1").
                withKind("RemoteEnvironment").
                withMetadataName("another-test2").
                withSpecDescription("Remote Environment for testing purpose").
                withSpecLabelsRegion("us").withSpecLabelsKind("production").build()
    }

    @Override
    String toString() {
        return "RemoteEnviroment{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}';
    }


    private class Metadata {
        private String name

        @Override
        String toString() {
            return "Metadata{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    private class Spec {
        private String description
        private Labels labels

        @Override
        String toString() {
            return "Spec{" +
                    "description='" + description + '\'' +
                    ", labels=" + labels +
                    '}'
        }

        private class Labels {
            private String region
            private String kind

            @Override
            String toString() {
                return "Labels{" +
                        "region='" + region + '\'' +
                        ", kind='" + kind + '\'' +
                        '}';
            }
        }
    }

    static class REBuilder {
        private RemoteEnviroment remoteEnviroment

        REBuilder() {
            remoteEnviroment = new RemoteEnviroment()
            remoteEnviroment.metadata = createMetadata(remoteEnviroment)
            remoteEnviroment.spec = createSpec(remoteEnviroment)
            remoteEnviroment.spec.labels = createSpecLabels(remoteEnviroment)
        }

        REBuilder withApiVersion(String apiVersion) {
            remoteEnviroment.apiVersion = apiVersion
            return this
        }

        REBuilder withKind(String kind) {
            remoteEnviroment.kind = kind
            return this
        }

        REBuilder withMetadataName(String name) {
            remoteEnviroment.metadata.name = name
            return this
        }

        REBuilder withSpecDescription(String description) {
            remoteEnviroment.spec.description = description
            return this
        }

        REBuilder withSpecLabelsRegion(String region) {
            remoteEnviroment.spec.labels.region = region
            return this
        }

        REBuilder withSpecLabelsKind(String kind) {
            remoteEnviroment.spec.labels.kind = kind
            return this
        }

        RemoteEnviroment build() {
            return remoteEnviroment;
        }
    }

}