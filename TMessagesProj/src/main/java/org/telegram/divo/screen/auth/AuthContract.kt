package org.telegram.divo.screen.auth

import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState
import org.telegram.divo.dal.dto.auth.LoginResponse

data class AuthViewState(
    val isLoading: Boolean = false,
    val loginResponse: LoginResponse? = null
) : ViewState

sealed class AuthViewIntent : ViewIntent {
    data object GoogleSignIn : AuthViewIntent()
}

sealed class AuthViewEffect : ViewEffect {
    data object LoginSuccess : AuthViewEffect()
    data object GoogleSignInRequested : AuthViewEffect()
    data class ShowError(val message: String) : AuthViewEffect()
}

