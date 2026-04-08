package org.telegram.divo.screen.similar_profiles.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SearchResultSection(
    imageUrl: String,
    topMatchesCount: Int,
    totalProfilesCount: Int,
    similarityPercent: Int,
    sortedType: String
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
            Row() {
                Text(
                    text = stringResource(R.string.SortedBy),
                    style = AppTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = AppTheme.colors.textPrimary.copy(0.6f)
                    )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = sortedType,
                    style = AppTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = AppTheme.colors.textPrimary
                    )
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}