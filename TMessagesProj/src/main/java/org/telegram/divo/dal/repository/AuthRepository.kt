package org.telegram.divo.dal.repository

import com.google.android.exoplayer2.util.Log
import org.telegram.divo.dal.network.AccessTokenProvider
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.dto.auth.LoginResponse
import org.telegram.divo.dal.dto.auth.RegistrationRequest

class AuthRepository(
    private val service: AuthService,
    private val accessTokenProvider: AccessTokenProvider
) {

    suspend fun login(request: LoginRequest): DivoResult<LoginResponse> {
        val result = resultOf { service.login(request) }
        Log.d("MyTag", "login")
        if (result is DivoResult.Success) {
            Log.d("MyTag", "success ${result.value.data?.accessToken}")
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
        }
        return result
    }

    suspend fun register(request: RegistrationRequest): DivoResult<LoginResponse> {
        val result = resultOf { service.registration(request) }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(result.value.data?.accessToken)
        }
        return result
    }

    suspend fun logout(): DivoResult<Unit> {
        val result = resultOf { service.logout() }
        if (result is DivoResult.Success) {
            accessTokenProvider.setAccessToken(null)
        }
        return result
    }
}

