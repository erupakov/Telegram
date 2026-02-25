package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.dto.publication.FeedResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Publication, feed, follower, and favorite related endpoints.
 */
interface PublicationService {

    @POST("feedline/list")
    suspend fun getFeed(
        @Body request: FeedRequestDto
    ): FeedResponse

    @POST("publication/like")
    suspend fun like(@Body body: Map<String, Any?>): ResponseBody

    @POST("publication/unlike")
    suspend fun unlike(@Body body: Map<String, Any?>): ResponseBody
}


