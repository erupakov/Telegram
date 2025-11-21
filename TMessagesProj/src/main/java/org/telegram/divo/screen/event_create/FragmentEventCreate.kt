package org.telegram.divo.screen.event_create

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment

class FragmentEventCreate : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent { CreateEventScreen() }
        return composeView
    }
}