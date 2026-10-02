package org.telegram.divo.screen.auth

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.common.arch.setDivoContent
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LoginActivity

class AuthFragment : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        fragmentView = ComposeView(context).apply {
            setDivoContent {
                AuthScreen(
                    onAuthClicked = { presentFragment(LoginActivity(), false) },
                    onGoogleSuccess = { authResponse -> 
                        val loginActivity = LoginActivity().setGoogleLoginSuccess(authResponse)
                        presentFragment(loginActivity, false, true)
                    },
                    onGoogleUserNotFound = { uid, email, dummyPhone, authResponse, firstName, lastName, photoUrl ->
                        val loginActivity = LoginActivity().setGoogleRegistrationParams(uid, email, dummyPhone, authResponse, true, firstName, lastName, photoUrl)
                        presentFragment(loginActivity, false)
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