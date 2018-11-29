package io.project.kyma.ace2e.model

class Metadata {

    private String name

    @Override
    String toString() {
        return "Metadata{" +
                "name='" + name + '\'' +
                '}'
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }
}
