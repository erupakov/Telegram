package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun CapacityCard(
    appliedCount: Int?,
    maxSpotsCount: Int?
) {
    val showApplied = appliedCount != null
    val showMaxSpots = maxSpotsCount != null && maxSpotsCount > 0

    if (!showApplied && !showMaxSpots) return

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showApplied) {
            CapacityItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_divo_applied,
                text = stringResource(R.string.EventApplied, appliedCount ?: 0)
            )
        }
        if (showMaxSpots) {
            CapacityItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_divo_max_spots,
                text = stringResource(R.string.EventMaxSpots, maxSpotsCount ?: 0)
            )
        }
    }
}

@Composable
private fun CapacityItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    text: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(14.dp))
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(14.dp))
    }
}