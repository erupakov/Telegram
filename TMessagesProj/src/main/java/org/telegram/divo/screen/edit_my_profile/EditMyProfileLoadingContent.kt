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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.shimmer

@Preview
@Composable
fun EditMyProfileLoadingContent() {
    val color = Color.White.copy(alpha = 0.05f)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(102.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer(highlightColor = color),
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer(highlightColor = color),
        )
    }
}