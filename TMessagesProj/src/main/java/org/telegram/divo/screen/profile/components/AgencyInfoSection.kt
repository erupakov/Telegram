package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AgencyInfoSection(
    title: String,
    photoUrl: String,
    onClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.blackAlpha12)
            .clickable { onClicked() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.CurrentAgency),
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.White,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(6.dp))
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
                    modifier = Modifier.size(24.dp).clip(CircleShape),
                    model = photoUrl,
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = stringResource(R.string.SeeHistory).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}