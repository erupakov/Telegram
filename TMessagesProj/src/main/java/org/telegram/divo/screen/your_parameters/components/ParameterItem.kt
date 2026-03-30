package org.telegram.divo.screen.your_parameters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ParameterItem(
    param: ProfileParameter?,
    onClick: (ParametersType) -> Unit,
) {
    param?.let {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clickableWithoutRipple { onClick(it.type) }
                .clip(RoundedCornerShape(41.dp))
                .background(AppTheme.colors.onBackground)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(0.6f).padding(top = 1.dp),
                text = stringResource(param.type.titleRes),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.weight(0.4f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                    text = it.value,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    modifier = Modifier.padding(top = 1.dp),
                    painter = painterResource(R.drawable.ic_divo_arrow_right_20),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
fun ParameterItemPreview() {
    ParameterItem(
        param = ProfileParameter(ParametersType.GENDER, "Female"),
        onClick = {}
    )
}