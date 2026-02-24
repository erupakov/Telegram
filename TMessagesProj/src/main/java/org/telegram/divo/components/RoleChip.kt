package org.telegram.divo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.telegram.messenger.R

@Composable
fun RoleChip(text: String) {
    val gradient = Brush.horizontalGradient(
        0.00f to Color(0xFFBB7148),
        0.22f to Color(0xFFD5B187),
        0.36f to Color(0xFFA46B4C),
        0.66f to Color(0xFF89503B),
        0.80f to Color(0xFF823F36),
        1.00f to Color(0xFFBB7148),
    )

    Row(
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .border(0.5.dp, Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_divo_model_icon),
            modifier = Modifier.size(12.dp),
            tint = Color.White,
            contentDescription = null
        )
        Spacer(Modifier.width(6.dp))
        androidx.compose.material.Text(text, color = Color.White)
    }
}