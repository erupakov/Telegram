package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun RequirementsCard(
    text: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = AppTheme.colors.onBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Text(
                text = stringResource(R.string.EventRequirements),
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary.copy(0.6f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = text,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}