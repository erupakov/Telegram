package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * System-level endpoints (feature flags, etc.).
 */
interface SystemService {

    @GET("system/feature-flags")
    suspend fun getFeatureFlags(): ResponseBody
}

