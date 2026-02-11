package org.telegram.divo.components

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.gms.vision.Frame

class SliderView(context: Context) : FrameLayout(context) {

    var OnAgeSelected: ((Int) -> Unit)? = null

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
                AgeSlider()
            }
        }
        addView(compose)
    }
}