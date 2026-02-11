package org.telegram.divo.dal.dao

import org.telegram.divo.dal.AccessTokenProvider
import org.telegram.divo.dal.DivoResult
import org.telegram.divo.dal.safeCall
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dto.auth.LoginRequest
import org.telegram.divo.dto.auth.LoginResponse
import org.telegram.divo.dto.auth.RegistrationRequest

class AuthDao(
    private val service: AuthService,
    private val accessTokenProvider: AccessTokenProvider
) {

    suspend fun login(request: LoginRequest): DivoResult<LoginResponse> {
        val result = safeCall { service.login(request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.data.accessToken)
        }
        return result
    }

    suspend fun register(request: RegistrationRequest): DivoResult<LoginResponse> {
        val result = safeCall { service.registration(request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.data.accessToken)
        }
        return result
    }

    suspend fun logout(): DivoResult<Unit> {
        val result = safeCall { service.logout() }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(null)
        }
        return result
    }
}

