package org.telegram.divo.screen.auth

import org.telegram.divo.common.BaseViewModel

class AuthViewModel :
    BaseViewModel<AuthViewState, AuthViewIntent, AuthViewEffect>() {

    override fun createInitialState(): AuthViewState = AuthViewState()

    override fun handleIntent(intent: AuthViewIntent) {
        when (intent) {
            is AuthViewIntent.GoogleSignIn -> {
                sendEffect(AuthViewEffect.GoogleSignInRequested)
            }
        }
    }
}

