package model

enum Definition {

    REMOTE_ENV("applicationconnector.kyma-project.io", "v1alpha1", "remoteenvironments")

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