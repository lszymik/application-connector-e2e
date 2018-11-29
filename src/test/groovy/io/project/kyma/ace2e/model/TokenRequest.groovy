package io.project.kyma.ace2e.model

class TokenRequest {

    private String apiVersion = "applicationconnector.kyma-project.io/v1alpha1"
    private String kind = "TokenRequest"
    private Metadata metadata

    TokenRequest(String appName) {
        def metadata = new Metadata()
        metadata.setName(appName)
        this.metadata = metadata
    }

    String getApiVersion() {
        return apiVersion
    }

    void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion
    }

    String getKind() {
        return kind
    }

    void setKind(String kind) {
        this.kind = kind
    }

    String getMetadataNem() {
        return metadata.name
    }

    void setMetadataName(String name) {
        this.metadata.name = name
    }
}
