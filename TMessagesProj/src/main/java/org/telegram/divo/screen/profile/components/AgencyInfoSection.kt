package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.entity.Agency
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AgencyInfoSection(
    agency: Agency?,
    onClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .clickable { onClicked() }
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (agency == null) 16.dp else 10.dp
            ),
    ) {
        if (agency == null) {
            ProfileInfoEmptyContent(
                R.drawable.ic_divo_experience_bage,
                stringResource(R.string.NotExperience),
            )
        } else {
            Text(
                text = stringResource(R.string.CurrentAgency),
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DivoAsyncImage(
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, AppTheme.colors.textPrimary.copy(0.6f), CircleShape)
                            .clip(CircleShape),
                        model = agency.photo?.fullUrl,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = agency.title,
                        style = AppTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = stringResource(R.string.SeeHistory).uppercase(),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}