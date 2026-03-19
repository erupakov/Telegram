package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun PlaceholderAvatar(
    modifier: Modifier = Modifier,
    name: String,
    isVisibleSmallIcon: Boolean = false,
    @DrawableRes smallIconResId: Int = R.drawable.ic_camera_add,
) {
    val initials = remember(name) {
        name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .joinToString("")
            .ifBlank { "A" }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.buttonColor,
            fontSize = 18.sp
        )

        if (isVisibleSmallIcon) {
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape),
                painter = painterResource(smallIconResId),
                contentDescription = null,
            )
        }
    }
}