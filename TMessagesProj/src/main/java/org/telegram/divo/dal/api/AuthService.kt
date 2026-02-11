package org.telegram.divo.dal.api

import org.telegram.divo.dto.auth.LoginRequest
import org.telegram.divo.dto.auth.LoginResponse
import org.telegram.divo.dto.auth.RegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth-related endpoints from the Divo backend.
 */
interface AuthService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/registration")
    suspend fun registration(@Body body: RegistrationRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}

