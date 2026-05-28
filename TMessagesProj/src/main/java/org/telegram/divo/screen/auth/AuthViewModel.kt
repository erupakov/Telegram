package org.telegram.divo.screen.auth

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.network.getErrorMessage

class AuthViewModel :
    BaseViewModel<AuthViewState, AuthViewIntent, AuthViewEffect>() {

    override fun createInitialState(): AuthViewState = AuthViewState()

    override fun handleIntent(intent: AuthViewIntent) {
        when (intent) {
            is AuthViewIntent.Login -> performLogin(
                email = intent.email,
                password = intent.password,
            )
            is AuthViewIntent.GoogleSignIn -> {
                sendEffect(AuthViewEffect.GoogleSignInRequested)
            }
        }
    }

    private fun performLogin(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            Log.d("VideoGrid", "performLogin before")
            setState { copy(isLoading = true) }

            val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            val model = Build.MODEL ?: "Android Device"
            val deviceId = "$manufacturer $model"
            val deviceType = "android"

            val request = LoginRequest(
                email = email,
                password = password,
                deviceId = deviceId,
                deviceType = deviceType
            )

            val result = DivoApi.authRepository.login(request)

            if (result is DivoResult.Success) {
                sendEffect(AuthViewEffect.LoginSuccess)
                DivoApi.accessTokenProvider.setGoogleLogin(false)
                delay(500)
                setState { copy(isLoading = false) }
            } else {
                setState { copy(isLoading = false) }
                sendEffect(AuthViewEffect.ShowError(result.getErrorMessage()))
            }
        }
    }
}

