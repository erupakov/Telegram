package org.telegram.divo.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.telegram.divo.style.AppTheme

data class PopupMenuItem(
    @StringRes val titleRes: Int,
    val onClick: () -> Unit,
)

@Composable
fun DivoPopupMenu(
    visible: Boolean,
    items: List<PopupMenuItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopEnd,
    offset: IntOffset = IntOffset(x = -32, y = 146),
) {
    if (!visible) return

    Popup(
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = modifier.width(250.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEEEEEE),
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(item.titleRes),
                                style = AppTheme.typography.manropeRegular,
                                fontSize = 17.sp,
                                color = Color.Black
                            )
                        },
                        onClick = {
                            onDismiss()
                            item.onClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 11.dp
                        )
                    )

                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xFFE0E0E0),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}