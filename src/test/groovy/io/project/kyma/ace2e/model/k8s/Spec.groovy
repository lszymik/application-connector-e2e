package io.project.kyma.ace2e.model.k8s

import com.google.gson.internal.LinkedTreeMap
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Spec {
	String description
	LinkedTreeMap<String, String> labels
	String region


	@Override
	String toString() {
		return "Spec{" +
				"description='" + description + '\'' +
				", labels=" + labels +
				", region='" + region + '\'' +
				'}';
	}
}
