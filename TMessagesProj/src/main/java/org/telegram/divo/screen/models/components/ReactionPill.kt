package org.telegram.divo.screen.models.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.telegram.divo.screen.models.Emotions

@Composable
fun ReactionPill(
    emotion: Emotions,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (emotion.selected) {
        Color(0xFFFFFFFF)
    } else {
        Color(0x3DFFFFFF)
    }

    Card(
        modifier = Modifier.size(height = 30.dp, width = 50.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val textColor = if (emotion.selected) {
                Color(0xFF222222)
            } else {
                Color(0xFFFFFFFF)
            }

            Text(emotion.emoji, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(Modifier.weight(1f))
            Text("${emotion.count}", color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}