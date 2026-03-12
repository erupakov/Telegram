package org.telegram.divo.screen.models

import android.content.Context
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity

class FragmentModels : BaseFragment() {

    private var modelsNavController: NavController? = null
    private var profileNavController: NavController? = null

    private val isOnHomeScreen = mutableStateOf(true)

    fun isOnHomeScreen(): Boolean = isOnHomeScreen.value

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        return ComposeView(context).apply {
            setContent {
                ModelsNavGraph(
                    onNavControllerReady = { navController ->
                        this@FragmentModels.modelsNavController = navController
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            isOnHomeScreen.value = destination.route == ModelsRoute.Models.route
                            (parentActivity as? LaunchActivity)?.updateBottomBarVisibility()
                        }
                    },
                    onInnerNavControllerReady = { navController ->
                        profileNavController = navController
                    }
                )
            }
        }
    }

    override fun isLightStatusBar(): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean {
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

        return super.onBackPressed()
    }
}