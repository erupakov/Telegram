package org.telegram.divo.screen.event_details

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.navigation.NavController
import org.telegram.divo.common.arch.setDivoContent
import org.telegram.divo.common.utils.FragmentLifecycleOwner
import org.telegram.ui.ActionBar.BaseFragment

class FragmentEventDetails : BaseFragment() {

    private val targetEventId: Int by lazy {
        arguments?.getInt(ARG_EVENT_ID, -1) ?: -1
    }

    private var navController: NavController? = null
    
    private val composeLifecycleOwner = FragmentLifecycleOwner().apply {
        onCreate()
        onStart()
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        composeLifecycleOwner.onDestroy()
        (fragmentView as? ComposeView)?.disposeComposition()
    }

    override fun onResume() {
        super.onResume()
        composeLifecycleOwner.onResume()
    }

    override fun onPause() {
        super.onPause()
        composeLifecycleOwner.onPause()
    }

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(composeLifecycleOwner)
            setViewTreeViewModelStoreOwner(composeLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(composeLifecycleOwner)
            setViewCompositionStrategy(object : ViewCompositionStrategy {
                override fun installFor(view: AbstractComposeView): () -> Unit {
                    return {}
                }
            })
            setDivoContent {
                CompositionLocalProvider(
                    LocalOnBackPressedDispatcherOwner provides composeLifecycleOwner
                ) {
                    EventDetailsNavGraph(
                        eventId = targetEventId,
                        isOwnProfile = false,
                        screenName = "EventDetails",
                        onNavigateBack = { finishFragment() },
                        onNavControllerReady = { nav -> navController = nav }
                    )
                }
            }
        }

        val container = object : FrameLayout(context) {
            override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
                var currentInsets = insets
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    currentInsets = child.dispatchApplyWindowInsets(currentInsets)
                }
                return currentInsets
            }
        }
        container.addView(composeView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        
        fragmentView = container
        return fragmentView
    }

    override fun isLightStatusBar(): Boolean {
        return true
    }

    override fun isSupportEdgeToEdge(): Boolean {
        return true
    }

    override fun onBackPressed(invoked: Boolean): Boolean {
        if (composeLifecycleOwner.onBackPressed()) {
            return false
        }
        return super.onBackPressed(invoked)
    }

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: Int): FragmentEventDetails {
            return FragmentEventDetails().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EVENT_ID, eventId)
                }
            }
        }
    }
}
