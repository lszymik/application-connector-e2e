package io.project.kyma.ace2e.model.k8s

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Subscription {
    String apiVersion
    String kind
    Metadata metadata
    Spec spec

    @Override
    String toString() {
        return "Subscription{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}'
    }

    static class Spec {
        String endpoint
        String event_type
        String event_type_version
        boolean include_subscription_name_header
        int max_inflight
        int push_request_timeout_ms
        String source_id


        @Override
        String toString() {
            return "Spec{" +
                    "endpoint='" + endpoint + '\'' +
                    ", event_type='" + event_type + '\'' +
                    ", event_type_version='" + event_type_version + '\'' +
                    ", include_subscription_name_header=" + include_subscription_name_header +
                    ", max_inflight=" + max_inflight +
                    ", push_request_timeout_ms=" + push_request_timeout_ms +
                    ", source_id='" + source_id + '\'' +
                    '}'
        }
    }
}
