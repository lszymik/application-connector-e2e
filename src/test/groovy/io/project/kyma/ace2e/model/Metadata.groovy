/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package io.project.kyma.ace2e.model

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
}
