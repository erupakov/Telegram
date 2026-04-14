package org.telegram.divo.screen.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun CityField(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickableWithoutRipple { onClick() }
            .clip(RoundedCornerShape(41.dp))
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            modifier = Modifier.padding(top = 1.dp),
            text = text.ifEmpty { stringResource(R.string.CityLabel) },
            color = if (text.isEmpty()) AppTheme.colors.textPrimary.copy(0.6f) else AppTheme.colors.textPrimary,
            style = AppTheme.typography.bodyLarge
        )
    }
}