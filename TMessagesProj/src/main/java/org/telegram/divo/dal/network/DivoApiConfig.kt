package org.telegram.divo.dal.network

/**
 * Central configuration for the Divo REST API client.
 *
 * The actual host is https://backend.divo.fashion and the OpenAPI declares /api as the base path.
 */
object DivoApiConfig {

    const val WEB_URL: String = "https://t.divo.global/"
    const val TERMS_URL = "https://www.divo.global/legal-documents/mobile-app-eula"
    const val PRIVACY_URL = "https://www.divo.global/legal-documents/privacy-policy"

    val BASE_URL: String
        get() = if (org.telegram.messenger.BuildVars.DEBUG_VERSION) {
            "https://api-stage.divo.fashion/api/"
        } else {
            "https://api.divo.fashion/v2/"
        }

    /**
     * Network timeouts (in seconds).
     */
    const val CONNECT_TIMEOUT_SECONDS: Long = 15
    const val READ_TIMEOUT_SECONDS: Long = 30
}

