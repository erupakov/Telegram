package org.telegram.divo.screen.work_create_edit

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.event_create.CreateEventScreen
import org.telegram.ui.ActionBar.BaseFragment

class FragmentWorkHistoryCreate : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent {
            CreateWorkHistoryScreen(
                onBack = {
                    finishFragment()
                }
            )
        }
        return composeView
    }
}