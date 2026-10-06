package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.asSharedFlow
import org.telegram.divo.dal.utils.AccessTokenProvider
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.dto.auth.SocialAuthResponse
import org.telegram.divo.dal.dto.auth.SocialLoginRequest
import org.telegram.divo.dal.dto.auth.SocialRegistrationRequest
import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.dto.auth.LoginResponse
import org.telegram.divo.dal.dto.auth.RegistrationRequest
import org.telegram.divo.dal.dto.auth.RegistrationResponse
import org.telegram.divo.dal.dto.auth.TelegramLinkRequest
import org.telegram.divo.dal.dto.auth.TelegramLinkResponse
import org.telegram.divo.dal.dto.auth.DummyPhoneResponse

class AuthRepository(
    private val service: AuthService,
    private val accessTokenProvider: AccessTokenProvider
) {

    private val _authStateFlow = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val authStateFlow = _authStateFlow.asSharedFlow()

    suspend fun login(request: LoginRequest): DivoResult<LoginResponse> {
        val result = resultOf { service.login(request) }
        if (result is DivoResult.Success) {
            saveToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun register(request: RegistrationRequest): DivoResult<RegistrationResponse> {
        val result = resultOf { service.registration(request) }
        if (result is DivoResult.Success) {
            val token = result.value.data?.accessToken ?: result.value.data?.token
            saveToken(token)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun linkTelegramAccount(request: TelegramLinkRequest): DivoResult<TelegramLinkResponse> {
        // Send the Divo token when we have one: without it the backend links by divoUserId only if the
        // profile phone matches the proof phone. No token -> no header (Retrofit omits null headers).
        val token = accessTokenProvider.getAccessToken()?.takeIf { it.isNotBlank() }
        val bearerToken = token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
        val result = resultOf { service.linkTelegramAccount(bearerToken, request) }
        if (result is DivoResult.Success) {
            saveToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun loginSocial(request: SocialLoginRequest): DivoResult<SocialAuthResponse> {
        val result = resultOf { service.loginSocial(request) }
        if (result is DivoResult.Success) {
            saveToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun registrationSocial(request: SocialRegistrationRequest): DivoResult<SocialAuthResponse> {
        val result = resultOf { service.registrationSocial(request) }
        if (result is DivoResult.Success) {
            saveToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun getDummyPhone(): DivoResult<DummyPhoneResponse> {
        return resultOf { service.getDummyPhone() }
    }

    /**
     * Invalidates [token] on the server. Local data is cleared by the caller beforehand
     * (see DivoLogoutHelper), so the result is informational only.
     */
    suspend fun logout(token: String): DivoResult<Unit> {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return resultOf { service.logout(bearerToken) }
    }

    // A success without a token must not wipe the stored one (that would silently log the user out)
    private fun saveToken(token: String?) {
        if (!token.isNullOrBlank()) accessTokenProvider.setAccessToken(token)
    }

    fun notifyLoggedOut() {
        _authStateFlow.tryEmit(false)
    }
}

