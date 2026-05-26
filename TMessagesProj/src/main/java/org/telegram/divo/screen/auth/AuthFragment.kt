package org.telegram.divo.screen.auth

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.style.setDivoContent
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LoginActivity

class AuthFragment : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        fragmentView = ComposeView(context).apply {
            setDivoContent {
                AuthScreen(
                    onAuthClicked = { presentFragment(LoginActivity(), true) },
                    onGoogleSuccess = { authResponse -> 
                        val loginActivity = LoginActivity().setGoogleLoginSuccess(authResponse)
                        presentFragment(loginActivity, true, true)
                    },
                    onGoogleUserNotFound = { uid, email, dummyPhone, authResponse ->
                        val loginActivity = LoginActivity().setGoogleRegistrationParams(uid, email, dummyPhone, authResponse, true)
                        presentFragment(loginActivity, true)
                    }
                )
            }
        }
        return fragmentView
    }

    override fun isLightStatusBar(): Boolean = true
    override fun isSupportEdgeToEdge(): Boolean = true
    override fun drawEdgeNavigationBar(): Boolean = false
}