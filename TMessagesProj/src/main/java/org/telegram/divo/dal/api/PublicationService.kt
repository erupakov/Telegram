package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Publication, feed, follower, and favorite related endpoints.
 */
interface PublicationService {

    @POST("publication/feed")
    suspend fun getFeed(@Body body: Map<String, Any?>): Response<ResponseBody>

    @POST("publication/like")
    suspend fun like(@Body body: Map<String, Any?>): Response<ResponseBody>

    @POST("publication/unlike")
    suspend fun unlike(@Body body: Map<String, Any?>): Response<ResponseBody>
}


