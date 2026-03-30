package org.telegram.divo.screen.event_filter

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.event_list.EventListScreen
import org.telegram.ui.ActionBar.BaseFragment

class FragmentEventFilter : BaseFragment() {

    override fun createView(context: Context): View {

        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent {
            EventFilterScreen(
                onBack = {
                    finishFragment()
                },
                onApply = {
                    finishFragment()
                },
                onBacK = {
                    finishFragment()
                }
            )
        }
        return composeView
    }
}