package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SettingsTopBar(
    onAction: () -> Unit,
    onQrCode: () -> Unit
) {
    val rawTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var statusBarPadding by remember { mutableStateOf(rawTopPadding) }
    if (rawTopPadding > statusBarPadding) {
        statusBarPadding = rawTopPadding
    }
    val topBarHeight = 56.dp

    Box(
        modifier = Modifier
            .height(statusBarPadding + topBarHeight)
            .fillMaxWidth()
            .padding(top = statusBarPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        RoundedButton(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart),
            resId = R.drawable.ic_qr_code,
            iconSize = 24.dp,
            iconTint = AppTheme.colors.textPrimary,
            onClick = onQrCode
        )
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.SettingsTitle).uppercase(),
            style = AppTheme.typography.appBar
        )
        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .align(Alignment.CenterEnd)
                .clickableWithoutRipple { onAction() },
            text = stringResource(R.string.EditBtn),
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 15.sp,
            color = AppTheme.colors.accentOrange
        )
    }
}