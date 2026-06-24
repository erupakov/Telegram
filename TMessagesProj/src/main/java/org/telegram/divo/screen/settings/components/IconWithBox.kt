package org.telegram.divo.screen.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme

@Composable
fun IconWithBox(
    @DrawableRes
    iconResId: Int
) {
    Box(
        Modifier
            .size(29.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.backgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = AppTheme.colors.textPrimary.copy(0.8f)
        )
    }
}