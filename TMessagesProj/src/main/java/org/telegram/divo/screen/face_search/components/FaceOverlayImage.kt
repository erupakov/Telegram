package org.telegram.divo.screen.face_search.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.components.shimmer
import org.telegram.divo.screen.face_search.FaceDetectionResult
import org.telegram.divo.style.AppTheme

@Composable
fun FaceOverlayImage(
    modifier: Modifier = Modifier,
    imageUri: String,
    detectionResult: FaceDetectionResult,
    isSearching: Boolean = false
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    val boxColor = when (detectionResult) {
        is FaceDetectionResult.Success -> Color(0xFFFF6B00)
        is FaceDetectionResult.NoFace  -> Color.Red
        else -> Color.Transparent
    }

    // Считаем доли смещения (0.0 - лево/верх, 0.5 - центр, 1.0 - право/низ)
    val (fractionX, fractionY) = remember(detectionResult) {
        if (detectionResult is FaceDetectionResult.Success && detectionResult.faces.isNotEmpty()) {
            val mainFace = detectionResult.faces.first().boundingBox
            val imageW = detectionResult.imageWidth.toFloat()
            val imageH = detectionResult.imageHeight.toFloat()

            val fX = (mainFace.centerX() / imageW).coerceIn(0f, 1f)
            val fY = (mainFace.centerY() / imageH).coerceIn(0f, 1f)

            fX to fY
        } else {
            0.5f to 0.5f // Если лиц нет - берем ровно центр
        }
    }

    // Alignment для картинки (он требует значения от -1.0 до 1.0)
    val faceAlignment = BiasAlignment(
        horizontalBias = fractionX * 2f - 1f,
        verticalBias = fractionY * 2f - 1f
    )

    Box(modifier = modifier) {
        DivoAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .aspectRatio(1f)
                .onSizeChanged { viewSize = it }
                .alpha(if (detectionResult is FaceDetectionResult.Loading) 0f else 1f),
            model = imageUri,
            contentScale = ContentScale.Crop,
            alignment = faceAlignment // Центрируем по лицу
        )

        if (detectionResult is FaceDetectionResult.Success && viewSize != IntSize.Zero && !isSearching) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                val imageWidth = detectionResult.imageWidth.toFloat()
                val imageHeight = detectionResult.imageHeight.toFloat()
                val viewWidth = viewSize.width.toFloat()
                val viewHeight = viewSize.height.toFloat()

                val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
                val scaledImageWidth = imageWidth * scale
                val scaledImageHeight = imageHeight * scale

                val offsetX = (viewWidth - scaledImageWidth) * fractionX
                val offsetY = (viewHeight - scaledImageHeight) * fractionY

                detectionResult.faces.forEach { face ->
                    val rect = face.boundingBox

                    val left = (rect.left * scale) + offsetX
                    val top = (rect.top * scale) + offsetY
                    val width = rect.width() * scale
                    val height = rect.height() * scale

                    val centerX = left + width / 2f
                    val centerY = top + height / 2f

                    val sideLength = maxOf(width, height) * 0.85f

                    // Вычисляем смещение вниз
                    val verticalShift = height * 0.06f

                    val sqLeft = centerX - sideLength / 2f
                    val sqTop = (centerY - sideLength / 2f) + verticalShift

                    val cornerRadius = 16.dp.toPx()
                    val cornerLength = sideLength * 0.20f

                    val path = Path().apply {
                        moveTo(sqLeft, sqTop + cornerLength)
                        lineTo(sqLeft, sqTop + cornerRadius)
                        quadraticTo(sqLeft, sqTop, sqLeft + cornerRadius, sqTop)
                        lineTo(sqLeft + cornerLength, sqTop)

                        moveTo(sqLeft + sideLength - cornerLength, sqTop)
                        lineTo(sqLeft + sideLength - cornerRadius, sqTop)
                        quadraticTo(sqLeft + sideLength, sqTop, sqLeft + sideLength, sqTop + cornerRadius)
                        lineTo(sqLeft + sideLength, sqTop + cornerLength)

                        moveTo(sqLeft + sideLength, sqTop + sideLength - cornerLength)
                        lineTo(sqLeft + sideLength, sqTop + sideLength - cornerRadius)
                        quadraticTo(sqLeft + sideLength, sqTop + sideLength, sqLeft + sideLength - cornerRadius, sqTop + sideLength)
                        lineTo(sqLeft + sideLength - cornerLength, sqTop + sideLength)

                        moveTo(sqLeft + cornerLength, sqTop + sideLength)
                        lineTo(sqLeft + cornerRadius, sqTop + sideLength)
                        quadraticTo(sqLeft, sqTop + sideLength, sqLeft, sqTop + sideLength - cornerRadius)
                        lineTo(sqLeft, sqTop + sideLength - cornerLength)
                    }

                    drawPath(
                        path = path,
                        color = boxColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
        }

        if (detectionResult is FaceDetectionResult.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .aspectRatio(1f)
                    .background(AppTheme.colors.onBackground)
                    .shimmer()
            )
        }

        if (isSearching) {
            val infiniteTransition = rememberInfiniteTransition(label = "scanner_transition")
            val scanFraction by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart // Начинаем заново сверху
                ),
                label = "scanner_animation"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp)) // Обрезаем по краям картинки
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val glowHeight = 60.dp.toPx() // Высота градиентного "хвоста" сканера

                // Высчитываем Y так, чтобы сканер плавно заходил и полностью выходил за экран
                val lineY = scanFraction * (canvasHeight + glowHeight)

                // 1. Рисуем полупрозрачный градиентный шлейф
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFF6B00).copy(alpha = 0.2f),
                            Color(0xFFFF6B00).copy(alpha = 0.6f)
                        ),
                        startY = lineY - glowHeight,
                        endY = lineY
                    ),
                    topLeft = Offset(x = 0f, y = lineY - glowHeight),
                    size = Size(width = canvasWidth, height = glowHeight)
                )

                // 2. Рисуем яркую горизонтальную линию
                drawLine(
                    color = Color(0xFFFF6B00), // Оранжевый цвет (как у рамки лица)
                    start = Offset(x = 0f, y = lineY),
                    end = Offset(x = canvasWidth, y = lineY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}