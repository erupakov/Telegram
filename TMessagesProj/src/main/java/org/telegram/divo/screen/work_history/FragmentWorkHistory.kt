package org.telegram.divo.screen.work_history

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.work_create_edit.FragmentWorkHistoryCreate
import org.telegram.ui.ActionBar.BaseFragment

class FragmentWorkHistory : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent {
            WorkHistoryScreen(
                onBack = {
                    finishFragment()
                },
                onCreateClicked = {
                    presentFragment(FragmentWorkHistoryCreate())
                }
            )
        }
        return composeView
    }
}