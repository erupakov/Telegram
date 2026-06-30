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
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun register(request: RegistrationRequest): DivoResult<RegistrationResponse> {
        val result = resultOf { service.registration(request) }
        if (result is DivoResult.Success) {
            val token = result.value.data?.accessToken ?: result.value.data?.token
            accessTokenProvider.setAccessToken(token)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun linkTelegramAccount(request: TelegramLinkRequest): DivoResult<TelegramLinkResponse> {
        val token = accessTokenProvider.getAccessToken() ?: ""
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = resultOf { service.linkTelegramAccount(bearerToken, request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun loginSocial(request: SocialLoginRequest): DivoResult<SocialAuthResponse> {
        val result = resultOf { service.loginSocial(request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun registrationSocial(request: SocialRegistrationRequest): DivoResult<SocialAuthResponse> {
        val result = resultOf { service.registrationSocial(request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
            _authStateFlow.tryEmit(true)
        }
        return result
    }

    suspend fun getDummyPhone(): DivoResult<DummyPhoneResponse> {
        return resultOf { service.getDummyPhone() }
    }

    suspend fun logout(): DivoResult<Unit> {
        val result = resultOf { service.logout() }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(null)
            _authStateFlow.tryEmit(false)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                DivoApi.userRepository.clearCache()
            }
        }
        return result
    }
}

