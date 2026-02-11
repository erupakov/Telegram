package org.telegram.divo.screen.auth

import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.divo.dto.auth.LoginResponse

data class AuthViewState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginResponse: LoginResponse? = null
) : ViewState

sealed class AuthViewIntent : ViewIntent {
    data class Login(
        val email: String,
        val password: String,
        val deviceId: String,
        val deviceType: String
    ) : AuthViewIntent()
}

sealed class AuthViewEffect : ViewEffect {
    data object LoginSuccess : AuthViewEffect()
    data class ShowError(val message: String) : AuthViewEffect()
}

