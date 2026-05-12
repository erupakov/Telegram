package org.telegram.divo.screen.auth

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.MainTabsActivity

class AuthFragment : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false) // Отключаем стандартный тулбар Telegram

        fragmentView = ComposeView(context).apply {
            setContent {
                AuthScreen(
                    onAuthClicked = { presentFragment(MainTabsActivity(), true) }
                )
            }
        }
        return fragmentView
    }

    override fun isLightStatusBar(): Boolean = true
    override fun isSupportEdgeToEdge(): Boolean = true
    override fun drawEdgeNavigationBar(): Boolean = false
}