package org.telegram.divo.screen.event_filter

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.style.setDivoContent
import org.telegram.ui.ActionBar.BaseFragment

class FragmentEventFilter : BaseFragment() {

    override fun createView(context: Context): View {

        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setDivoContent {
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