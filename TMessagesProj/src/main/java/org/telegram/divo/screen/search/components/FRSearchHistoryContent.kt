package org.telegram.divo.screen.search.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun FRSearchHistoryContent(
    history: List<FaceRecognitionEntity>,
    onSeeAllClicked: () -> Unit,
    onHistoryItemClicked: (FaceRecognitionEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.RecentFaceSearches),
                style = AppTheme.typography.helveticaNeueLtCom,
                color = AppTheme.colors.textPrimary,
                fontSize = 16.sp,
            )
            Text(
                modifier = Modifier.clickableWithoutRipple { onSeeAllClicked() },
                text = stringResource(R.string.SeeAll),
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.accentOrange,
                fontSize = 15.sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        history.take(3).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithoutRipple { onHistoryItemClicked(item) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DivoAsyncImage(
                    model = item.imageUri.toUri(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${item.resultsCount} ${pluralStringResource(R.plurals.ResultFor, item.resultsCount)} ${stringResource(R.string.Found)}",
                        style = AppTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val formattedDate = remember(item.createdAt) {
                        java.text.SimpleDateFormat(
                            "EEEE, d MMM 'at' HH:mm", 
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(item.createdAt))
                    }
                    
                    Text(
                        text = formattedDate,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary.copy(0.6f)
                    )
                }
            }
        }
    }
}
