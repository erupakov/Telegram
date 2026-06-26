package org.telegram.divo.dal.network

/**
 * Central configuration for the Divo REST API client.
 *
 * The actual host is https://backend.divo.fashion and the OpenAPI declares /api as the base path.
 */
object DivoApiConfig {

    const val WEB_URL: String = "https://t.divo.global/"

    val BASE_URL: String
        get() = if (org.telegram.messenger.BuildVars.DEBUG_VERSION) {
            "https://api-stage.divo.fashion/api/"
        } else {
            "https://api-stage.divo.fashion/api/"//"https://api.divo.fashion/v2/"
        }

    /**
     * Network timeouts (in seconds).
     */
    const val CONNECT_TIMEOUT_SECONDS: Long = 15
    const val READ_TIMEOUT_SECONDS: Long = 30
}

