package org.telegram.divo.screen.similar_profiles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SearchResultSection(
    imageUrl: String,
    topMatchesCount: Int,
    totalProfilesCount: Int,
    similarityPercent: Int,
    hasActiveFilters: Boolean,
    activeFiltersCount: Int,
    onReset: () -> Unit
) {
    Column {
        val countText = pluralStringResource(
            id = R.plurals.ResultsCount,
            count = topMatchesCount,
            topMatchesCount
        )

        val text = stringResource(
            id = R.string.ResultsWithSimilarity,
            countText,
            similarityPercent
        )

        ResultRow(
            imageUrl = imageUrl,
            result = text
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = pluralStringResource(
                    id = R.plurals.ProfilesFound,
                    count = totalProfilesCount,
                    totalProfilesCount
                ),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 16.sp,
                color = AppTheme.colors.textPrimary
            )
            Row {
                if (hasActiveFilters) {
                    ActiveFiltersChip(
                        activeFiltersCount = activeFiltersCount,
                        onReset = onReset
                    )
                } else {
                    Text(
                        text = stringResource(R.string.SortedBy),
                        style = AppTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = AppTheme.colors.textPrimary.copy(0.6f)
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.SortedByMatch),
                        style = AppTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun ActiveFiltersChip(
    activeFiltersCount: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.SortedByFilters),
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f)
            )
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = activeFiltersCount.toString(),
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = AppTheme.colors.textPrimary
            )
        )

        Spacer(Modifier.width(10.dp))

        Icon(
            modifier = Modifier
                .size(9.dp)
                .clickableWithoutRipple { onReset() },
            painter = painterResource(R.drawable.ic_divo_close),
            contentDescription = null,
            tint = AppTheme.colors.textPrimary
        )
    }
}