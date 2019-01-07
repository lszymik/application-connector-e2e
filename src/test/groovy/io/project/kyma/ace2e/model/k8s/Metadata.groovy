package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Metadata {
	String clusterName
	String creationTimestamp
	int generation
	String name
	String namespace
	String resourceVersion
	String selfLink
	String uid

	@Override
	String toString() {
		return "Metadata{" +
				"clusterName='" + clusterName + '\'' +
				", creationTimestamp='" + creationTimestamp + '\'' +
				", generation=" + generation +
				", name='" + name + '\'' +
				", namespace='" + namespace + '\'' +
				", resourceVersion='" + resourceVersion + '\'' +
				", selfLink='" + selfLink + '\'' +
				", uid='" + uid + '\'' +
				'}';
	}
}
