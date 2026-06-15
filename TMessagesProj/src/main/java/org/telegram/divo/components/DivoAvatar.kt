package org.telegram.divo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import org.telegram.messenger.R
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.style.AppTheme

@Composable
fun DivoAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    isOnline: Boolean = false,
    showBorder: Boolean = true,
    avatarSize: androidx.compose.ui.unit.Dp = 64.dp
) {
    Box(
        modifier = modifier.size(avatarSize)
    ) {
        val onBackgroundColor = AppTheme.colors.onBackground
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (showBorder) {
                        val strokeWidth = 2.dp.toPx()

                        drawCircle(
                            color = onBackgroundColor,
                            radius = size.minDimension / 2f - strokeWidth / 2f,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Левая часть с градиентом
                        val gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF180800), Color(0xFFFF5C02)),
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height)
                        )
                        
                        drawArc(
                            brush = gradient,
                            startAngle = 90f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
        ) {
            DivoAsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape),
                model = imageUrl,
                contentScale = ContentScale.Crop,
                errorContent = {
                    Image(
                        painter = painterResource(R.drawable.divo_avatar_placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            )
        }

        if (isOnline) {
            val badgeSize = 14.dp
            val borderSize = 2.dp
            val cornerOffset = avatarSize * 0.1465f - badgeSize / 2
            
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = -cornerOffset, y = -cornerOffset)
                    .background(Color(0xFF1EDD4E), CircleShape)
                    .border(borderSize, Color.White, CircleShape)
            )
        }
    }
}
