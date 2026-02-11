package org.telegram.divo.dal

/**
 * Central configuration for the Divo REST API client.
 *
 * The actual host is https://backend.divo.fashion and the OpenAPI declares /api as the base path.
 */
object DivoApiConfig {

    /**
     * Base URL for Retrofit. Must end with a trailing slash.
     */
    const val BASE_URL: String = "https://backend.divo.fashion/api/"

    /**
     * Network timeouts (in seconds).
     */
    const val CONNECT_TIMEOUT_SECONDS: Long = 15
    const val READ_TIMEOUT_SECONDS: Long = 30
}

