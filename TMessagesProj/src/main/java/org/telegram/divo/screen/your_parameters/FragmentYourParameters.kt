package org.telegram.divo.screen.your_parameters

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment

class FragmentYourParameters : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent {
            YourParametersScreen(
                onSaved = {
                    finishFragment()
                },
                onBack = {
                    finishFragment()
                }
            )
        }
        return composeView
    }
}