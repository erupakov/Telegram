package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.dto.auth.LoginResponse
import org.telegram.divo.dal.dto.auth.RegistrationRequest
import org.telegram.divo.dal.dto.auth.RegistrationResponse
import org.telegram.divo.dal.dto.auth.SocialAuthResponse
import org.telegram.divo.dal.dto.auth.SocialLoginRequest
import org.telegram.divo.dal.dto.auth.SocialRegistrationRequest
import org.telegram.divo.dal.dto.auth.TelegramLinkRequest
import org.telegram.divo.dal.dto.auth.TelegramLinkResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Auth-related endpoints from the Divo backend.
 */
interface AuthService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/registration")
    suspend fun registration(@Body body: RegistrationRequest): RegistrationResponse

    @POST("auth/telegram-link")
    suspend fun linkTelegramAccount(
        @Header("Authorization") authHeader: String,
        @Body body: TelegramLinkRequest
    ): TelegramLinkResponse

    @POST("auth/login-social")
    suspend fun loginSocial(@Body body: SocialLoginRequest): SocialAuthResponse

    @POST("auth/registration-social")
    suspend fun registrationSocial(@Body body: SocialRegistrationRequest): SocialAuthResponse

    @POST("auth/logout")
    suspend fun logout()

    @retrofit2.http.GET("auth/dummy_phone")
    suspend fun getDummyPhone(): org.telegram.divo.dal.dto.auth.DummyPhoneResponse
}

