package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.dto.publication.FeedResponse
import org.telegram.divo.dal.dto.publication.FeedlineSearchRequest
import org.telegram.divo.dal.dto.publication.FeedlineSearchResponse
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

    @POST("feedline/search")
    suspend fun searchFeedline(
        @Body request: FeedlineSearchRequest
    ): FeedlineSearchResponse

    @POST("publication/like")
    suspend fun like(@Body body: Map<String, Any?>): ResponseBody

    @POST("publication/unlike")
    suspend fun unlike(@Body body: Map<String, Any?>): ResponseBody
}


