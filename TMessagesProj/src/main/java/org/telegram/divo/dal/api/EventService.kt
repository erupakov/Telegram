package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Event-related endpoints from the Divo backend.
 */
interface EventService {

    @POST("event/list")
    suspend fun listEvents(@Body body: Map<String, Any?>): ResponseBody

    @GET("event/{id}")
    suspend fun getEvent(@Path("id") id: Long): ResponseBody
}


