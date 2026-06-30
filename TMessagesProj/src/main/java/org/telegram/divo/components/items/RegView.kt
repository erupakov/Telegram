package org.telegram.divo.components.items

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.inputs.UIButton

class RegButtonView(context: Context, text: String) : FrameLayout(context) {

    var onClick: (() -> Unit)? = null
    private val isLoadingState = mutableStateOf(false)

    fun setLoading(loading: Boolean) {
        isLoadingState.value = loading
    }

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
                RegButtonView(
                    text = text,
                    isLoading = isLoadingState.value,
                    onClick = {
                        onClick?.invoke()
                    }
                )
            }
        }
        addView(compose)
    }
}

@Preview
@Composable
fun RegButtonView(
    text: String = "Continue",
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp)
    ) {
        UIButton(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isLoading = isLoading,
            onClick = onClick
        )
    }
}
