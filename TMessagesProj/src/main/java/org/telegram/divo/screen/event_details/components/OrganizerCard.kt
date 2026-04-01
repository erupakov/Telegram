package org.telegram.divo.screen.event_details.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun OrganizerCard(
    avatarUrl: String?,
    name: String,
    status: String,
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        color = AppTheme.colors.onBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.OrganizationLabel),
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary.copy(0.6f)
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Аватар-заглушка
                DivoAsyncImage(
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    model = avatarUrl,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "@$name",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = status,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary.copy(0.6f)
                    )
                }
            }
        }
    }
}