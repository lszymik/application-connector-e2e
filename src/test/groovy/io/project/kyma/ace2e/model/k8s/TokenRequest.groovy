package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class TokenRequest {
	String apiVersion
	String kind
	Metadata metadata
	Status status

	class Status {
		String expireAfter
		String application
		String token
		String state
		String url


		@Override
		String toString() {
			return "Status{" +
					"expireAfter='" + expireAfter + '\'' +
					", application='" + application + '\'' +
					", token='" + token + '\'' +
					", state='" + state + '\'' +
					", url='" + url + '\'' +
					'}'
		}
	}

	@Override
	String toString() {
		return "TokenRequest{" +
				"apiVersion='" + apiVersion + '\'' +
				", kind='" + kind + '\'' +
				", metadata=" + metadata +
				", status=" + status +
				'}'
	}

}
