package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.common.EmptyResponse
import org.telegram.divo.dal.dto.publication.CreatePublicationRequest
import org.telegram.divo.dal.dto.publication.CreatePublicationResponse
import org.telegram.divo.dal.dto.publication.FavoriteRequest
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.dto.publication.FeedResponse
import org.telegram.divo.dal.dto.publication.FeedlineSearchRequest
import org.telegram.divo.dal.dto.publication.FeedlineSearchResponse
import org.telegram.divo.dal.dto.publication.PublicationListRequest
import org.telegram.divo.dal.dto.publication.PublicationListResponse
import org.telegram.divo.dal.dto.publication.LikeRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

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

    @POST("publication/list")
    suspend fun getPublicationList(
        @Body request: PublicationListRequest
    ): PublicationListResponse

    @POST("publication/create")
    suspend fun createPublication(
        @Body request: CreatePublicationRequest
    ): CreatePublicationResponse

    @DELETE("publication/{id}")
    suspend fun deletePublication(
        @Path("id") id: Int
    ): EmptyResponse

    @POST("feedline/like")
    suspend fun likePost(@Body request: LikeRequest): EmptyResponse

    @POST("feedline/unlike")
    suspend fun unlikePost(@Body request: LikeRequest): EmptyResponse

    @POST("favorite/mark")
    suspend fun markFavorite(@Body request: FavoriteRequest): EmptyResponse

    @POST("favorite/unmark")
    suspend fun unmarkFavorite(@Body request: FavoriteRequest): EmptyResponse
}


