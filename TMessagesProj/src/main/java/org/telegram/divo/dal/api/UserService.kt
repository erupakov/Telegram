package org.telegram.divo.dal.api

import okhttp3.MultipartBody
import org.telegram.divo.dal.dto.common.EmptyResponse
import org.telegram.divo.dal.dto.common.UserSocialNetworkDto
import org.telegram.divo.dal.dto.user.AddGalleryRequest
import org.telegram.divo.dal.dto.user.AddToGalleryResponse
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.AgencyModelsResponse
import org.telegram.divo.dal.dto.user.AgencySearchRequest
import org.telegram.divo.dal.dto.user.AgencySearchResponse
import org.telegram.divo.dal.dto.user.AddAgencyModelRequest
import org.telegram.divo.dal.dto.user.AppearancesResponse
import org.telegram.divo.dal.dto.user.EngagementResponse
import org.telegram.divo.dal.dto.user.UpdateProfileAgencyRequest
import org.telegram.divo.dal.dto.user.UpdateProfileRequest
import org.telegram.divo.dal.dto.user.UploadFileResponse
import org.telegram.divo.dal.dto.user.UploadFilesResponse
import org.telegram.divo.dal.dto.user.UpsertSocialNetworkRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.UserGalleryListResponse
import org.telegram.divo.dal.dto.user.UserInfoResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @POST("agency/update")
    suspend fun updateAgency(
        @Body body: UpdateProfileAgencyRequest
    ): EmptyResponse

    @POST("user-gallery/list")
    suspend fun getUserGalleryList(
        @Body request: UserGalleryListRequest
    ): UserGalleryListResponse

    @DELETE("user-gallery/{id}")
    suspend fun deleteFromGallery(
        @Path("id") id: Int
    ): EmptyResponse

    @POST("agency/{agencyId}/models/list")
    suspend fun getAgencyModels(
        @Path("agencyId") agencyId: Int,
        @Body request: AgencyModelsRequest
    ): AgencyModelsResponse

    @POST("agency/search")
    suspend fun searchAgencyModels(
        @Body request: AgencySearchRequest
    ): AgencySearchResponse

    @POST("agency/{userId}/models")
    suspend fun addAgencyModel(
        @Path("userId") userId: Int,
        @Body request: AddAgencyModelRequest
    ): EmptyResponse

    @DELETE("agency/{agencyId}/models/{modelId}")
    suspend fun deleteAgencyModel(
        @Path("agencyId") agencyId: Int,
        @Path("modelId") modelId: Int
    ): EmptyResponse

    @Multipart
    @POST("file/upload-file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadFileResponse

    @Multipart
    @POST("file/upload-files")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>
    ): UploadFilesResponse

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
        @Query("userId") userId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("search") search: String,
    ): EngagementResponse

    @GET("dictionary/appearances")
    suspend fun getAppearances(): AppearancesResponse
}


