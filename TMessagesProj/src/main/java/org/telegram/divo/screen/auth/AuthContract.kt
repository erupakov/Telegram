package org.telegram.divo.screen.auth

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.dal.dto.auth.LoginResponse

data class AuthViewState(
    val isLoading: Boolean = false,
    val loginResponse: LoginResponse? = null
) : ViewState

sealed class AuthViewIntent : ViewIntent {
    data class Login(
        val email: String,
        val password: String,
    ) : AuthViewIntent()
}

sealed class AuthViewEffect : ViewEffect {
    data object LoginSuccess : AuthViewEffect()
    data class ShowError(val message: String) : AuthViewEffect()
}

