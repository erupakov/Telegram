package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.user.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * User-related endpoints from the Divo backend.
 */
interface UserService {

    @GET("user/info")
    suspend fun getCurrentUserInfo(): UserInfoResponse

    /**
     * The request body schema is loosely defined in OpenAPI, so we accept a map of fields.
     */
    @POST("user/update-profile")
    suspend fun updateProfile(@Body body: Map<String, Any?>): UserInfoResponse
}


