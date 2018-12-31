package io.project.kyma.ace2e.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class TokenRequest {

    String apiVersion
    String kind
    Metadata metadata
    Spec spec
	String expireAfter
	String application
	String state
	String type
	String url
}
