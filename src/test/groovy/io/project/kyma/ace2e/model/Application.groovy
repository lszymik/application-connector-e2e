package io.project.kyma.ace2e.model

class Application {

    private String apiVersion
    private String kind
    private Metadata metadata
    private Spec spec

    private static createMetadata() {
        return new Metadata()
    }

    private static createSpec(Application application) {
        return new Spec(application)
    }

    private static createSpecLabels(Application application) {
        return new Spec.Labels(application.spec)
    }

    static Application buildTestApp() {
        def builder = new AppBuilder()
        builder.withApiVersion("applicationconnector.kyma-project.io/v1alpha1").
                withKind("Application").
                withMetadataName("test-app-e2e").
                withSpecDescription("Application for testing purpose").
                withSpecLabelsRegion("us").withSpecLabelsKind("production").build()
    }

    String getMetadataName() {
        return metadata.name
    }

    @Override
    String toString() {
        return "Application{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}'
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
                        '}'
            }
        }
    }

    static class AppBuilder {
        private Application application

        AppBuilder() {
            application = new Application()
            application.metadata = createMetadata()
            application.spec = createSpec(application)
            application.spec.labels = createSpecLabels(application)
        }

        AppBuilder withApiVersion(String apiVersion) {
            application.apiVersion = apiVersion
            return this
        }

        AppBuilder withKind(String kind) {
            application.kind = kind
            return this
        }

        AppBuilder withMetadataName(String name) {
            application.metadata.name = name
            return this
        }

        AppBuilder withSpecDescription(String description) {
            application.spec.description = description
            return this
        }

        AppBuilder withSpecLabelsRegion(String region) {
            application.spec.labels.region = region
            return this
        }

        AppBuilder withSpecLabelsKind(String kind) {
            application.spec.labels.kind = kind
            return this
        }

        Application build() {
            return application
        }
    }

}