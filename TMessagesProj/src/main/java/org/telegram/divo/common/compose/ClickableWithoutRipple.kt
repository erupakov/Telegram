package org.telegram.divo.common.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.clickableWithoutRipple(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}

@androidx.compose.foundation.ExperimentalFoundationApi
fun Modifier.combinedClickableWithoutRipple(
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    combinedClickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onLongClick = onLongClick,
        onClick = onClick
    )
}