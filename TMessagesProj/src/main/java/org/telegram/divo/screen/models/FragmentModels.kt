package org.telegram.divo.screen.models

import android.content.Context
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.divo.common.utils.DivoDeeplinkDispatcher
import org.telegram.divo.style.setDivoContent
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.MainTabsActivity
import org.telegram.ui.MainTabsActivityController

class FragmentModels : BaseFragment(), MainTabsActivity.TabFragmentDelegate {

    private var modelsNavController: NavController? = null
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
            setDivoContent {
                ModelsNavGraph(
                    onNavControllerReady = { navController ->
                        this@FragmentModels.modelsNavController = navController
                        DivoDeeplinkDispatcher.modelsNavController = navController
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            isOnHomeScreen.value = destination.route == ModelsRoute.Models.route
                            mainTabsController?.setTabsVisible(isOnHomeScreen.value)
                            mainTabsController?.setModelsSearchVisible(isOnHomeScreen.value)
                        }
                    },
                    onInnerNavControllerReady = { navController ->
                        profileNavController = navController
                    },
                    onNavigateToChat = { tgId, tgHash, tgUsername ->
                        val currentAccount = org.telegram.messenger.UserConfig.selectedAccount
                        var user = org.telegram.messenger.MessagesController.getInstance(currentAccount).getUser(tgId)
                        if (user == null) {
                            user = org.telegram.tgnet.TLRPC.TL_user()
                            user.id = tgId
                            user.first_name = tgUsername ?: "User"
                            user.username = tgUsername
                            user.access_hash = tgHash ?: 0L
                            org.telegram.messenger.MessagesController.getInstance(currentAccount).putUser(user, false)
                        }
                        val args = android.os.Bundle()
                        args.putLong("user_id", tgId)
                        presentFragment(org.telegram.ui.ChatActivity(args))
                    }
                )
            }
        }
        return fragmentView
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

        val outer = modelsNavController
        if (outer != null && outer.previousBackStackEntry != null) {
            outer.popBackStack()
            return false
        }

        return super.onBackPressed(invoked)
    }

    fun openSearchFromBottomBar() {
        modelsNavController?.navigate(ModelsRoute.Search.route)
    }
}