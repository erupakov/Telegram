package org.telegram.divo.screen.event_list

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.event_create.FragmentEventCreate
import org.telegram.divo.screen.event_details.FragmentEventDetails
import org.telegram.ui.ActionBar.BaseFragment

class FragmentEventList : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context)
        composeView.setContent {
            EventListScreen(
                onNavigateToEventDetails = {
                    presentFragment(FragmentEventDetails())
                },
                onNavigateToCreateEvent = {
                    presentFragment(FragmentEventCreate())
                },
                onNavigateToSearch = {
                    // TODO: hook up search once destination is ready
                }
            )
        }
        return composeView
    }

}