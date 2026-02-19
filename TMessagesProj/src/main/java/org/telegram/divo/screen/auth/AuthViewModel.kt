package org.telegram.divo.screen.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.dto.auth.LoginRequest

class AuthViewModel :
    BaseViewModel<AuthViewState, AuthViewIntent, AuthViewEffect>() {

    override fun createInitialState(): AuthViewState = AuthViewState()

    override fun handleIntent(intent: AuthViewIntent) {
        when (intent) {
            is AuthViewIntent.Login -> performLogin(
                email = intent.email,
                password = intent.password,
                deviceId = intent.deviceId,
                deviceType = intent.deviceType
            )
        }
    }

    private fun performLogin(
        email: String,
        password: String,
        deviceId: String,
        deviceType: String
    ) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            val request = LoginRequest(
                email = email,
                password = password,
                deviceId = deviceId,
                deviceType = deviceType
            )

            when (val result = DivoApi.authRepository.login(request)) {
                is DivoResult.Success -> {
                    setState { copy(isLoading = false, loginResponse = result.value) }
                    sendEffect(AuthViewEffect.LoginSuccess)
                }

                is DivoResult.HttpError -> {
                    val message = result.body?.message ?: "HTTP ${result.code}"
                    setState { copy(isLoading = false, errorMessage = message) }
                    sendEffect(AuthViewEffect.ShowError(message))
                }

                is DivoResult.NetworkError -> {
                    val message = result.exception.localizedMessage ?: "Network error"
                    setState { copy(isLoading = false, errorMessage = message) }
                    sendEffect(AuthViewEffect.ShowError(message))
                }

                is DivoResult.UnknownError -> {
                    val message = result.throwable.localizedMessage ?: "Unknown error"
                    setState { copy(isLoading = false, errorMessage = message) }
                    sendEffect(AuthViewEffect.ShowError(message))
                }
            }
        }
    }
}

