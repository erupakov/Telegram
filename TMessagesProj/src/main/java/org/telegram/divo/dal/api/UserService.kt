package org.telegram.divo.dal.api

import okhttp3.MultipartBody
import org.telegram.divo.dal.dto.common.UserSocialNetworkDto
import org.telegram.divo.dal.dto.user.AddGalleryRequest
import org.telegram.divo.dal.dto.user.AddToGalleryResponse
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.AgencyModelsResponse
import org.telegram.divo.dal.dto.user.EngagementResponse
import org.telegram.divo.dal.dto.user.UpdateProfileRequest
import org.telegram.divo.dal.dto.user.UploadFileResponse
import org.telegram.divo.dal.dto.user.UpsertSocialNetworkRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.UserGalleryListResponse
import org.telegram.divo.dal.dto.user.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("user/update-profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): UserInfoResponse

    @POST("user-gallery/list")
    suspend fun getUserGalleryList(
        @Body request: UserGalleryListRequest
    ): UserGalleryListResponse

    @POST("agency/{agencyId}/models/list")
    suspend fun getAgencyModels(
        @Path("agencyId") agencyId: Int,
        @Body request: AgencyModelsRequest
    ): AgencyModelsResponse

    @Multipart
    @POST("file/upload-file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadFileResponse

    @POST("user-gallery/add")
    suspend fun addToGallery(
        @Body request: AddGalleryRequest
    ): AddToGalleryResponse

    @POST("user-social-network/upsert")
    suspend fun upsertSocialNetwork(
        @Body request: UpsertSocialNetworkRequest
    )

    @GET("user-social-network")
    suspend fun getUserSocialNetworks(): List<UserSocialNetworkDto>

    @GET("user/engagement")
    suspend fun getEngagement(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): EngagementResponse
}


