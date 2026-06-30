package org.telegram.divo.screen.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.messenger.UserConfig
import org.telegram.divo.common.arch.setDivoContent
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ChangeUsernameActivity
import org.telegram.ui.ChatActivity
import org.telegram.ui.DataSettingsActivity
import org.telegram.ui.LanguageSelectActivity
import org.telegram.ui.LogoutActivity
import org.telegram.ui.MainTabsActivityController
import org.telegram.ui.NotificationsSettingsActivity
import org.telegram.ui.PrivacySettingsActivity
import org.telegram.ui.ThemeActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import org.telegram.divo.common.utils.FragmentLifecycleOwner
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.ChatObject
import org.telegram.ui.Components.UndoView
import org.telegram.ui.Components.LayoutHelper
import android.widget.FrameLayout
import android.view.Gravity
import android.view.ViewGroup
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.dal.network.DivoApi
import org.telegram.messenger.MessagesController
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ChannelCreateActivity

class FragmentSettings : BaseFragment(), NotificationCenter.NotificationCenterDelegate {

    private val composeLifecycleOwner = FragmentLifecycleOwner().apply {
        onCreate()
        onStart()
        onResume()
    }

    private var settingsNavController: NavController? = null
    private var profileNavController: NavController? = null

    private val isOnHomeScreen = mutableStateOf(true)
    private var mainTabsController: MainTabsActivityController? = null

    fun setMainTabsActivityController(controller: MainTabsActivityController) {
        this.mainTabsController = controller
    }

    private var undoView: UndoView? = null

    override fun onFragmentCreate(): Boolean {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.needDeleteDialog)
        return super.onFragmentCreate()
    }

    override fun createView(context: Context): View {
        if (fragmentView != null) return fragmentView
        actionBar.setAddToContainer(false)
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(composeLifecycleOwner)
            setViewTreeViewModelStoreOwner(composeLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(composeLifecycleOwner)
            setViewCompositionStrategy(object : androidx.compose.ui.platform.ViewCompositionStrategy {
                override fun installFor(view: androidx.compose.ui.platform.AbstractComposeView): () -> Unit {
                    return {} // Prevent disposal on detach
                }
            })
            setDivoContent {
                CompositionLocalProvider(
                    LocalOnBackPressedDispatcherOwner provides composeLifecycleOwner
                ) {
                SettingsNavGraph(
                    navigateToSavedMessages = { openSavedMessages() },
                    navigateToNotifications = { presentFragment(NotificationsSettingsActivity()) },
                    navigateToPrivacy = { presentFragment(PrivacySettingsActivity()) },
                    navigateToDataStorage = { presentFragment(DataSettingsActivity()) },
                    navigateToAppearance = { presentFragment(ThemeActivity(ThemeActivity.THEME_TYPE_BASIC)) },
                    navigateToSetUsername = { presentFragment(ChangeUsernameActivity()) },
                    navigateToLanguage = { presentFragment(LanguageSelectActivity()) },
                    onNavControllerReady = { navController ->
                        this@FragmentSettings.settingsNavController = navController
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            isOnHomeScreen.value = destination.route == SettingsRoute.Settings.route
                            mainTabsController?.setTabsVisible(isOnHomeScreen.value)
                        }
                    },
                    onInnerNavControllerReady = { navController ->
                        profileNavController = navController
                    },
                    onNavigateToChat = { tgId, tgHash, tgUsername ->
                        val currentAccount = UserConfig.selectedAccount
                        var user = MessagesController.getInstance(currentAccount).getUser(tgId)
                        if (user == null) {
                            user = TLRPC.TL_user()
                            user.id = tgId
                            user.first_name = tgUsername ?: "User"
                            user.username = tgUsername
                            user.access_hash = tgHash ?: 0L
                            MessagesController.getInstance(currentAccount).putUser(user, false)
                        }
                        val args = Bundle()
                        args.putLong("user_id", tgId)
                        presentFragment(ChatActivity(args))
                    },
                    onNavigateToCreateChannel = {
                        val args = Bundle()
                        args.putInt("step", 0)
                        presentFragment(ChannelCreateActivity(args))
                    },
                    navigateToLogout = {
                        val activity = parentActivity
                        if (activity != null) {
                            showDialog(LogoutActivity.makeLogOutDialog(activity, currentAccount))
                        }
                    }
                )
                }
            }
        }
        
        val container = FrameLayout(context)
        container.addView(composeView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        
        undoView = UndoView(context, this, false, null)
        undoView?.elevation = 50f
        container.addView(undoView, LayoutHelper.createFrame(
            LayoutHelper.MATCH_PARENT, 
            LayoutHelper.WRAP_CONTENT.toFloat(), 
            Gravity.BOTTOM or Gravity.LEFT, 8f, 0f, 8f, 8f
        ))
        
        fragmentView = container
        return fragmentView
    }

    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any) {
        if (id == NotificationCenter.needDeleteDialog) {
            val dialogId = args[0] as Long
            val user = args[1] as? org.telegram.tgnet.TLRPC.User
            val chat = args[2] as? org.telegram.tgnet.TLRPC.Chat
            val revoke = if (user != null && user.bot) false else args[3] as Boolean
            val botBlock = if (user != null && user.bot) args[3] as Boolean else false

            val deleteRunnable = Runnable {
                val currentUserId = DivoApi.userRepository.currentUserFlow.value?.id ?: 0
                if (chat != null) {
                    if (ChatObject.isChannel(chat)) {
                        DivoAnalytics.logEvent(AnalyticsEvent.ChannelDeleteSuccess(currentUserId, dialogId))
                    }
                    if (ChatObject.isNotInChat(chat)) {
                        MessagesController.getInstance(currentAccount).deleteDialog(dialogId, 0, revoke)
                    } else {
                        MessagesController.getInstance(currentAccount).deleteParticipantFromChat(-dialogId, MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).clientUserId), null, revoke, revoke)
                    }
                } else {
                    MessagesController.getInstance(currentAccount).deleteDialog(dialogId, 0, revoke)
                    if (user != null && user.bot && botBlock) {
                        MessagesController.getInstance(currentAccount).blockPeer(user.id)
                    }
                }
                MessagesController.getInstance(currentAccount).checkIfFolderEmpty(0)
            }

            if (!ChatObject.isForum(chat)) {
                undoView?.showWithAction(dialogId, if (revoke) UndoView.ACTION_DELETE else UndoView.ACTION_LEAVE, deleteRunnable)
            } else {
                deleteRunnable.run()
            }
        }
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

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.needDeleteDialog)
        composeLifecycleOwner.onDestroy()
        (fragmentView as? ComposeView)?.disposeComposition()
    }
}