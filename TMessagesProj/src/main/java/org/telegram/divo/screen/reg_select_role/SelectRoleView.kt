package org.telegram.divo.screen.reg_select_role

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.telegram.ui.Components.SlideView

class RoleSelectionView(context: Context) : SlideView(context) {

    var onContinue: ((Role) -> Unit)? = null
    var onSelect: ((Role) -> Unit)? = null

    init {
        val compose = ComposeView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                RoleSelectionHost(
                    onSelect = { role -> onSelect?.invoke(role) },
                    onContinue = { role -> onContinue?.invoke(role) }
                )
            }
        }
        addView(compose)
    }
}
