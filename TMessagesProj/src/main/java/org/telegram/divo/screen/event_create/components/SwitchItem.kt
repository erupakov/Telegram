package org.telegram.divo.screen.event_create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme

@Composable
fun SwitchItem(
    text: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(46.dp))
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .padding(top = 1.dp),
            text = text,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Switch(
            modifier = Modifier
                .scale(0.8f)
                .height(24.dp),
            checked = checked,
            onCheckedChange = onChanged,
            thumbContent = {
                Box(
                    modifier = Modifier
                        .size(23.dp)
                        .background(AppTheme.colors.onBackground, CircleShape)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Transparent,
                checkedTrackColor = AppTheme.colors.accentOrange,
                uncheckedThumbColor = Color.Transparent,
                uncheckedTrackColor = AppTheme.colors.backgroundLight,
                uncheckedBorderColor = Color.Transparent,
            )
        )
    }
}