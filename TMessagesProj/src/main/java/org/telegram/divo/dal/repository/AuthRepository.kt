package org.telegram.divo.dal.repository

import org.telegram.divo.dal.network.AccessTokenProvider
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.dto.auth.LoginResponse
import org.telegram.divo.dal.dto.auth.RegistrationRequest
import org.telegram.divo.dal.dto.auth.RegistrationResponse
import org.telegram.divo.dal.dto.auth.TelegramLinkRequest
import org.telegram.divo.dal.dto.auth.TelegramLinkResponse

class AuthRepository(
    private val service: AuthService,
    private val accessTokenProvider: AccessTokenProvider
) {

    suspend fun login(request: LoginRequest): DivoResult<LoginResponse> {
        val result = resultOf { service.login(request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
        }
        return result
    }

    suspend fun register(request: RegistrationRequest): DivoResult<RegistrationResponse> {
        val result = resultOf { service.registration(request) }
        if (result is DivoResult.Success) {
            val token = result.value.data?.accessToken ?: result.value.data?.token
            accessTokenProvider.setAccessToken(token)
        }
        return result
    }

    suspend fun linkTelegramAccount(request: TelegramLinkRequest): DivoResult<TelegramLinkResponse> {
        val token = accessTokenProvider.getAccessToken() ?: ""
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        val result = resultOf { service.linkTelegramAccount(bearerToken, request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
        }
        return result
    }

    suspend fun logout(): DivoResult<Unit> {
        val result = resultOf { service.logout() }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(null)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                DivoApi.userRepository.clearCache()
            }
        }
        return result
    }
}

