package org.telegram.divo.common.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.util.concurrent.atomic.AtomicInteger

private val activeEffectCount = AtomicInteger(0)

@Composable
fun StatusBarIconColorEffect(useDarkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(view, useDarkIcons) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        activeEffectCount.incrementAndGet()
        insetsController.isAppearanceLightStatusBars = useDarkIcons
        onDispose {
            if (activeEffectCount.decrementAndGet() == 0) {
                insetsController.isAppearanceLightStatusBars = true
            }
        }
    }
}
