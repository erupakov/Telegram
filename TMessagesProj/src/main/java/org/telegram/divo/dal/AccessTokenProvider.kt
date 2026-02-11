package org.telegram.divo.dal

/**
 * Abstraction for providing and persisting the JWT access token used with the Divo backend.
 *
 * This is intentionally minimal so it can be backed by SharedPreferences, an existing
 * auth helper, or any other storage that fits the app.
 */
interface AccessTokenProvider {

    /**
     * Returns the current access token, or null if the user is not authenticated.
     */
    suspend fun getAccessToken(): String?

    /**
     * Persists the latest access token. Passing null should clear any stored token.
     */
    suspend fun setAccessToken(token: String?)
}

