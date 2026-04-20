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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    fx: Float? = null,
    fy: Float? = null,
    onReset: () -> Unit
) {
    val imageAlignment = remember(fx, fy) {
        if (fx != null && fy != null) {
            object : Alignment {
                override fun align(
                    size: androidx.compose.ui.unit.IntSize,
                    space: androidx.compose.ui.unit.IntSize,
                    layoutDirection: androidx.compose.ui.unit.LayoutDirection
                ): androidx.compose.ui.unit.IntOffset {
                    var offsetX = space.width / 2f - fx * size.width.toFloat()
                    var offsetY = space.height / 2f - fy * size.height.toFloat()

                    val minOffsetX = space.width.toFloat() - size.width.toFloat()
                    val minOffsetY = space.height.toFloat() - size.height.toFloat()

                    offsetX = offsetX.coerceIn(minOffsetX, 0f)
                    offsetY = offsetY.coerceIn(minOffsetY, 0f)

                    return androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt())
                }
            }
        } else {
            Alignment.Center
        }
    }

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
            result = text,
            alignment = imageAlignment
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