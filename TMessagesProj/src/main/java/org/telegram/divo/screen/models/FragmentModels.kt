package org.telegram.divo.screen.models

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment

class FragmentModels : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context)
        composeView.setContent {
            ModelsHomeScreen(
                onSearch = {}
            )
        }
        return composeView
    }
}