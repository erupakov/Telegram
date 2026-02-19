package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.dto.auth.LoginResponse
import org.telegram.divo.dal.dto.auth.RegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth-related endpoints from the Divo backend.
 */
interface AuthService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/registration")
    suspend fun registration(@Body body: RegistrationRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout()
}

