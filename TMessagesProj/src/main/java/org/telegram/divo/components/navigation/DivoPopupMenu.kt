package org.telegram.divo.components.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

data class PopupMenuItem(
    @field:StringRes val titleRes: Int,
    val onClick: () -> Unit,
    @field:DrawableRes val iconRes: Int? = null,
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    if (!visible) return

    Popup(
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalContext provides context,
            androidx.compose.ui.platform.LocalConfiguration provides configuration
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
                        val color = if (item.titleRes == R.string.BlockProfile || item.titleRes == R.string.DeleteEvent || item.titleRes == R.string.DeleteProfile) {
                            Color.Red
                        } else {
                            AppTheme.colors.textPrimary
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(item.titleRes),
                                    style = AppTheme.typography.manropeRegular,
                                    fontSize = 16.sp,
                                    color = color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                            ),
                            trailingIcon = item.iconRes?.let {
                                {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(it),
                                        contentDescription = null,
                                        tint = color
                                    )
                                }
                            }
                        )

                        if (index == 0 && items.size > 1 && item.iconRes != null && item.titleRes != R.string.DeleteEvent) {
                            HorizontalDivider(
                                color = Color.LightGray,
                                thickness = 7.dp
                            )
                        } else if (index < items.lastIndex) {
                            HorizontalDivider(
                                color = Color.LightGray,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}