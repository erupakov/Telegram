package org.telegram.divo.screen.models

import android.content.Context
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.divo.common.utils.DivoDeeplinkDispatcher
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
        actionBar.setAddToContainer(false)

        fragmentView = ComposeView(context).apply {
            setContent {
                ModelsNavGraph(
                    onNavControllerReady = { navController ->
                        this@FragmentModels.modelsNavController = navController
                        DivoDeeplinkDispatcher.modelsNavController = navController
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            isOnHomeScreen.value = destination.route == ModelsRoute.Models.route
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
}