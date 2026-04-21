package org.telegram.divo.screen.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ChangeUsernameActivity
import org.telegram.ui.ChatActivity
import org.telegram.ui.DataSettingsActivity
import org.telegram.ui.MainTabsActivityController
import org.telegram.ui.NotificationsSettingsActivity
import org.telegram.ui.PrivacySettingsActivity
import org.telegram.ui.ThemeActivity

class FragmentSettings : BaseFragment() {

    private var settingsNavController: NavController? = null
    private var profileNavController: NavController? = null

    private val isOnHomeScreen = mutableStateOf(true)
    private var mainTabsController: MainTabsActivityController? = null

    fun setMainTabsActivityController(controller: MainTabsActivityController) {
        this.mainTabsController = controller
    }

    override fun createView(context: Context): View {
        if (fragmentView != null) return fragmentView
        actionBar.setAddToContainer(false)
        fragmentView = ComposeView(context).apply {
            setContent {
                SettingsNavGraph(
                    navigateToSavedMessages = { openSavedMessages() },
                    navigateToNotifications = { presentFragment(NotificationsSettingsActivity()) },
                    navigateToPrivacy = { presentFragment(PrivacySettingsActivity()) },
                    navigateToDataStorage = { presentFragment(DataSettingsActivity()) },
                    navigateToAppearance = { presentFragment(ThemeActivity(ThemeActivity.THEME_TYPE_BASIC)) },
                    navigateToSetUsername = { presentFragment(ChangeUsernameActivity()) },
                    onNavControllerReady = { navController ->
                        this@FragmentSettings.settingsNavController = navController
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            isOnHomeScreen.value = destination.route == SettingsRoute.Settings.route
                            mainTabsController?.setTabsVisible(isOnHomeScreen.value)
                        }
                    },
                    onInnerNavControllerReady = { navController ->
                        profileNavController = navController
                    }
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

    override fun isLightStatusBar(): Boolean {
        return true
    }

    override fun isSupportEdgeToEdge(): Boolean {
        return true
    }

    override fun drawEdgeNavigationBar(): Boolean {
        return false
    }

    override fun onBackPressed(invoked: Boolean): Boolean {
        val inner = profileNavController
        if (inner != null && inner.previousBackStackEntry != null) {
            inner.popBackStack()
            return false
        }

        val outer = settingsNavController
        if (outer != null && outer.previousBackStackEntry != null) {
            outer.popBackStack()
            return false
        }

        return super.onBackPressed(invoked)
    }
}