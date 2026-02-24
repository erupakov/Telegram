package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.UserGalleryListResponse
import org.telegram.divo.dal.dto.user.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * User-related endpoints from the Divo backend.
 */
interface UserService {

    @GET("user/info")
    suspend fun getCurrentUserInfo(): UserInfoResponse

    @GET("user/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: Int
    ): UserInfoResponse

    @POST("user-gallery/list")
    suspend fun getUserGalleryList(
        @Body request: UserGalleryListRequest
    ): UserGalleryListResponse
}


