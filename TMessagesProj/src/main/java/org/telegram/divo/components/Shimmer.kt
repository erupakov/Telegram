package org.telegram.divo.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.shimmer(
    isLoading: Boolean = true,
    shimmerColor: Color = Color.Black.copy(alpha = 0.1f),
    highlightColor: Color = Color.White.copy(alpha = 0.3f),
    cornerRadius: Dp = 0.dp,
): Modifier = this.then(
    if (!isLoading) Modifier
    else Modifier.composed {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )

        val brush = Brush.linearGradient(
            colors = listOf(shimmerColor, highlightColor, shimmerColor),
            start = Offset(translateAnim - 300f, 0f),
            end = Offset(translateAnim, 0f)
        )

        background(brush = brush, shape = RoundedCornerShape(cornerRadius))
    }
)