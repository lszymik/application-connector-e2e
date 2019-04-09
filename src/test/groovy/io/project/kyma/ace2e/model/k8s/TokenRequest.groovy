package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class TokenRequest {
	String apiVersion
	String kind
	Metadata metadata
	Status status
	Context context

	@Override
	String toString() {
		return "TokenRequest{" +
				"apiVersion='" + apiVersion + '\'' +
				", kind='" + kind + '\'' +
				", metadata=" + metadata +
				", status=" + status +
				", context=" + context +
				'}'
	}

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
					", applicationName='" + application + '\'' +
					", token='" + token + '\'' +
					", state='" + state + '\'' +
					", url='" + url + '\'' +
					'}'
		}
	}

	class Context {
		String group
		String tenant


		@Override
		String toString() {
			return "Context{" +
					"group='" + group + '\'' +
					", tenant='" + tenant + '\'' +
					'}'
		}
	}

}
