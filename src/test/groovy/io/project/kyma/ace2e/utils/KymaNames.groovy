package io.project.kyma.ace2e.utils

/**
 * Helper for creating an unique resource names.
 */
class KymaNames {

    /**
     * Production namespace.
     */
    static final String PRODUCTION_NAMESPACE = "production"

    /**
     * Integration namespace.
     */
    static final String INTEGRATION_NAMESPACE = "kyma-integration"

    public static final String STATUS_DEPLOYED = "DEPLOYED"

    /**
     * Returns random string
     *
     * @return random string with the length of 9.
     */
    static String getRandomString() {
        new Random().with { (1..9).collect { (('a'..'z')).join()[nextInt((('a'..'z')).join().length())] }.join() }
    }
}
