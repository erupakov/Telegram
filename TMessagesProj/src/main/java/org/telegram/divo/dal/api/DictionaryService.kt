package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * Dictionary endpoints for static reference data.
 */
interface DictionaryService {

    @GET("dictionary/gender")
    suspend fun getGenders(): ResponseBody

    @GET("dictionary/appearances")
    suspend fun getAppearances(): ResponseBody

    @GET("dictionary/payments")
    suspend fun getPayments(): ResponseBody
}

