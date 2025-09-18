package org.telegram.divo.items

import android.content.Context
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.TextItemDescription
import org.telegram.divo.components.UIButton
import org.telegram.divo.style.AppColors
import org.telegram.divo.style.AppTheme

class RegView {
}

class RegButtonView(context: Context, text: String) : FrameLayout(context) {

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
                RegButtonView(
                    text = text,
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
    text:String = "Continue",
    onClick:()-> Unit = {}
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        var checked by remember { mutableStateOf(true) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked,
                onCheckedChange = { checked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.colors.switchTumb,
                    uncheckedThumbColor = AppTheme.colors.switchTumb,
                    uncheckedTrackColor = AppTheme.colors.switchBackgroundUnChecked,
                    checkedTrackColor = AppTheme.colors.switchBackgroundChecked,
                )
            )
            Spacer(Modifier.size(16.dp))
            TextItemDescription(
                text = "By using DIVO, you agree to Apple’s end User License Agreement and our Terms of Service and Privacy Policy.",
                color = AppTheme.colors.textColor
            )

        }
        Spacer(Modifier.size(16.dp))
        UIButton(
            text =text,
            modifier = Modifier.fillMaxWidth(),
            enabled = checked,
            onClick = onClick
        )
    }
}
