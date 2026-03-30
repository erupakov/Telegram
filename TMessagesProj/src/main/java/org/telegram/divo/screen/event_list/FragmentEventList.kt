package org.telegram.divo.screen.event_list

import android.content.Context
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.divo.screen.event_create.FragmentEventCreate
import org.telegram.divo.screen.event_details.FragmentEventDetails
import org.telegram.divo.screen.event_filter.FragmentEventFilter
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.MainTabsActivityController

class FragmentEventList : BaseFragment() {

    private var eventsNavController: NavController? = null
    private var detailNavController: NavController? = null

    private val isOnHomeScreen = mutableStateOf(true)

    private var mainTabsController: MainTabsActivityController? = null

    fun setMainTabsActivityController(controller: MainTabsActivityController) {
        this.mainTabsController = controller
    }

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        fragmentView = ComposeView(context).apply {
            setContent {
                EventsNavGraph(
                    onNavControllerReady = { navController ->
                        this@FragmentEventList.eventsNavController = navController
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            isOnHomeScreen.value = destination.route == EventRoute.Events.route
                            mainTabsController?.setTabsVisible(isOnHomeScreen.value)
                        }
                    },
                    onInnerNavControllerReady = { navController ->
                        detailNavController = navController
                    },
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
        val inner = detailNavController
        if (inner != null && inner.previousBackStackEntry != null) {
            inner.popBackStack()
            return false
        }

        val outer = eventsNavController
        if (outer != null && outer.previousBackStackEntry != null) {
            outer.popBackStack()
            return false
        }

        return super.onBackPressed(invoked)
    }
}