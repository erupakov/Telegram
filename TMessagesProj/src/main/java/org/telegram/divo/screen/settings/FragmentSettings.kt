package org.telegram.divo.screen.settings

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.profile.FragmentProfileN
import org.telegram.divo.screen.your_parameters.FragmentYourParameters
import org.telegram.ui.ActionBar.BaseFragment

class FragmentSettings : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent {
            SettingsScreen(
                navigateToFillParameters = { presentFragment(FragmentYourParameters()) },
                navigateToProfile = {
                    presentFragment(FragmentProfileN())
                }
            )
        }
        return composeView
    }
}