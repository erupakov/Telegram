package org.telegram.divo.screen.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun EmptyPlaceContent(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp)
            .clip(RoundedCornerShape(41.dp))
            .background(AppTheme.colors.onBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_divo_search),
                contentDescription = null,
                tint = AppTheme.colors.textPrimary.copy(0.8f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.NoItemsFound, title),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.TryDifferent, body),
            style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.colors.textPrimary.copy(0.8f)
            ),
        )
    }
}