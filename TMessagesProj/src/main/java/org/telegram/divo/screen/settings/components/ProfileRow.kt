package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.media.DivoAsyncImage
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ProfileRow(
    name: String,
    phone: String,
    avatar: String,
) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DivoAsyncImage(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.onBackground),
            model = avatar,
            errorContent = {
                Image(
                    painter = painterResource(R.drawable.divo_avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        if (name.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = name,
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.textPrimary,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
        }
        if (phone.isNotEmpty() && !DivoApi.accessTokenProvider.isGoogleLogin() && !phone.startsWith("999")) {
            Text(
                text = "+${phone}",
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.textPrimary.copy(0.8f),
                fontSize = 14.sp,
            )
        }
    }
}