package org.telegram.divo.screen.profile_social_links

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.edit_my_profile.EditMyProfileScreen
import org.telegram.ui.ActionBar.BaseFragment

class FragmentProfileSocialLinks : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context)
        composeView.setContent {
            ProfileSocialLinksScreen(
                onCloseScreen = {
                    finishFragment()
                })
        }
        return composeView
    }

}