package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * Dictionary endpoints for static reference data.
 */
interface DictionaryService {

    @GET("dictionary/gender")
    suspend fun getGenders(): Response<ResponseBody>

    @GET("dictionary/appearances")
    suspend fun getAppearances(): Response<ResponseBody>

    @GET("dictionary/payments")
    suspend fun getPayments(): Response<ResponseBody>
}

