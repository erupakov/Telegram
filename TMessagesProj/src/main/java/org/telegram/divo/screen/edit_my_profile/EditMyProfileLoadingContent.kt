package org.telegram.divo.screen.edit_my_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.shimmer

@Composable
fun EditMyProfileLoadingContent(
    isModel: Boolean
) {
    val color = Color.White.copy(alpha = 0.05f)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(32.dp))
        // First name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(41.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(16.dp))
        // Last name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(41.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(38.dp)) // Label + space
        // Biography
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(114.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(16.dp))
        // Country
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(41.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(16.dp))
        // City
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(41.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(20.dp))
        // Save button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(41.dp))
                .shimmer(highlightColor = color),
        )
    }
}