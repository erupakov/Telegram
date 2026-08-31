package org.telegram.divo.screen.your_parameters.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.RoundedButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersTopBar(
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Text(
                text = stringResource(R.string.YourParameters).uppercase(),
                style = AppTheme.typography.appBar,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            RoundedButton(
                modifier = Modifier.padding(start = 16.dp),
                resId = R.drawable.ic_divo_back,
                onClick = onBack
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.backgroundLight
        )
    )
}