package org.telegram.divo.screen.event_list

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.event_create.FragmentEventCreate
import org.telegram.divo.screen.event_details.FragmentEventDetails
import org.telegram.divo.screen.event_filter.FragmentEventFilter
import org.telegram.ui.ActionBar.BaseFragment

class FragmentEventList : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        fragmentView = ComposeView(context).apply {
            setContent {
                EventListScreen(
                    onNavigateToEventDetails = {
                        presentFragment(FragmentEventDetails())
                    },
                    onNavigateToCreateEvent = {
                        presentFragment(FragmentEventCreate())
                    },
                    onNavigateToSearch = {
                        presentFragment(FragmentEventFilter())

                    }
                )
            }
        }
        return fragmentView
    }

    override fun isSupportEdgeToEdge(): Boolean {
        return true
    }

    override fun drawEdgeNavigationBar(): Boolean {
        return false
    }
}