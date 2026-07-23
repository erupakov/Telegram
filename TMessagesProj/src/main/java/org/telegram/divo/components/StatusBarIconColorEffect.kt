package org.telegram.divo.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.util.concurrent.atomic.AtomicInteger

private val activeEffectCount = AtomicInteger(0)

@Composable
fun StatusBarIconColorEffect(useDarkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    val isResumed = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)

    DisposableEffect(view, useDarkIcons, isResumed) {
        if (!isResumed) {
            return@DisposableEffect onDispose {}
        }

        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        
        // Save previous state by checking Telegram's theme
        val isDarkTheme = org.telegram.ui.ActionBar.Theme.getCurrentTheme().isDark()
        val isActionBarWhite = org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_actionBarDefault) == android.graphics.Color.WHITE
        val defaultLightStatusBar = isActionBarWhite || !isDarkTheme

        activeEffectCount.incrementAndGet()
        
        // Apply our custom effect. Profile needs a transparent status bar.
        insetsController.isAppearanceLightStatusBars = useDarkIcons
        org.telegram.messenger.AndroidUtilities.setLightStatusBar(window, useDarkIcons, true)

        onDispose {
            if (activeEffectCount.decrementAndGet() == 0) {
                // Restore Telegram's default status bar behavior for MainTabsActivity
                insetsController.isAppearanceLightStatusBars = defaultLightStatusBar
                org.telegram.messenger.AndroidUtilities.setLightStatusBar(window, defaultLightStatusBar, false)
            }
        }
    }
}
