package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.screen.settings.SettingsItem
import org.telegram.divo.screen.settings.SettingsViewModel
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    viewModel: SettingsViewModel,
    value: String? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.onBackground)
            .clickableWithoutRipple(onClick = { viewModel.setIntent(item.intent) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconWithBox(
            iconResId = item.iconResId
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.title,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        if (value != null) {
            Text(
                text = value,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            painter = painterResource(R.drawable.ic_divo_arrow_right_20),
            contentDescription = null,
            tint = AppTheme.colors.backgroundDark
        )
    }
}