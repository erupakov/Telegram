package org.telegram.divo.screen.profile

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.edit_my_profile.FragmentEditMyProfile
import org.telegram.divo.screen.event_create.FragmentEventCreate
import org.telegram.divo.screen.event_details.FragmentEventDetails
import org.telegram.divo.screen.event_list.EventListScreen
import org.telegram.ui.ActionBar.BaseFragment

class FragmentProfileN : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context)
        composeView.setContent {
            ProfileScreen(
                onEditClicked = {
                    presentFragment(FragmentEditMyProfile())
                },
                onNavigateBack = {
                    finishFragment()
                },
                onEditLinksClicked = {
                    presentFragment(FragmentEditMyProfile())
                }
            )
        }
        return composeView
    }

}