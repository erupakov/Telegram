package org.telegram.divo.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onBackClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .clickableWithoutRipple { onBackClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back_21),
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = stringResource(R.string.ButtonBack),
            style = AppTheme.typography.manropeRegular,
            fontSize = 17.sp,
            color = color,
        )
    }
}