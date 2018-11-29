package io.project.kyma.ace2e.model

enum Definition {

    APP_ENV("applicationconnector.kyma-project.io", "v1alpha1", "applications"),
    TOKEN_REQ("applicationconnector.kyma-project.io", "v1alpha1", "tokenrequests")

    private String group
    private String version
    private String plural

    Definition(group, version, plural) {
        this.group = group
        this.version = version
        this.plural = plural
    }

    String getGroup() {
        return group
    }

    String getVersion() {
        return version
    }

    String getPlural() {
        return plural
    }
}