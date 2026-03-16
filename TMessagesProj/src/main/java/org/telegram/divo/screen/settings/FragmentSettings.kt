package org.telegram.divo.screen.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.profile.FragmentProfileN
import org.telegram.divo.screen.your_parameters.FragmentYourParameters
import org.telegram.messenger.MediaDataController
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ChatActivity
import org.telegram.ui.DataSettingsActivity
import org.telegram.ui.NotificationsSettingsActivity
import org.telegram.ui.PrivacySettingsActivity
import org.telegram.ui.StickersActivity
import org.telegram.ui.ThemeActivity
import org.telegram.ui.ChangeUsernameActivity

class FragmentSettings : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        fragmentView = ComposeView(context).apply {
            setContent {
                SettingsScreen(
                    navigateToFillParameters = { presentFragment(FragmentYourParameters()) },
                    navigateToProfile = { presentFragment(FragmentProfileN.newInstance(it, true)) },
                    navigateToSavedMessages = { openSavedMessages() },
                    navigateToStickers = { presentFragment(StickersActivity(MediaDataController.TYPE_IMAGE, null)) },
                    navigateToNotifications = { presentFragment(NotificationsSettingsActivity()) },
                    navigateToPrivacy = { presentFragment(PrivacySettingsActivity()) },
                    navigateToDataStorage = { presentFragment(DataSettingsActivity()) },
                    navigateToAppearance = { presentFragment(ThemeActivity(ThemeActivity.THEME_TYPE_BASIC)) },
                    navigateToSetUsername = { presentFragment(ChangeUsernameActivity()) }
                )
            }
        }
        return fragmentView
    }

    private fun openSavedMessages() {
        val args = Bundle()
        val userId = UserConfig.getInstance(currentAccount).getClientUserId()
        args.putLong("user_id", userId)
        presentFragment(ChatActivity(args))
    }

    override fun isSupportEdgeToEdge(): Boolean {
        return true
    }

    override fun drawEdgeNavigationBar(): Boolean {
        return false
    }
}