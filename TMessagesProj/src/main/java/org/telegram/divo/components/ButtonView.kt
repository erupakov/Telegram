package org.telegram.divo.components

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.gms.vision.Frame

class ButtonView(context: Context) : FrameLayout(context) {

    var onClick: (() -> Unit)? = null

    init {
        val compose = ComposeView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                Column (Modifier.fillMaxWidth()){
                    Spacer(modifier = Modifier.weight(1f))
                    UIButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onClick?.invoke()
                        }
                    )
                }
            }
        }
        addView(compose)
    }
}