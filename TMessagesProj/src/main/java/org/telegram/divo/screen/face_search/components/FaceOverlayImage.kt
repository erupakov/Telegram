package org.telegram.divo.screen.face_search.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.components.shimmer
import org.telegram.divo.screen.face_search.FaceDetectionResult
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun FaceOverlayImage(
    modifier: Modifier = Modifier,
    imageUri: String,
    detectionResult: FaceDetectionResult,
    isSearching: Boolean = false,
    selectedFaceIndex: Int? = null,
    onFaceClick: (Int) -> Unit = {}
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .aspectRatio(1f)
            .onSizeChanged { viewSize = it }
            .alpha(if (detectionResult is FaceDetectionResult.Loading) 0f else 1f)
            .pointerInput(detectionResult, viewSize, isSearching) {
                detectTapGestures { tapOffset ->
                    // Кликать можно только если лиц больше 1 и нет активного поиска
                    if (detectionResult is FaceDetectionResult.Success && !isSearching && viewSize != IntSize.Zero && detectionResult.faces.size > 1) {
                        val imageW = detectionResult.imageWidth.toFloat()
                        val imageH = detectionResult.imageHeight.toFloat()
                        val vW = viewSize.width.toFloat()
                        val vH = viewSize.height.toFloat()

                        val scale = maxOf(vW / imageW, vH / imageH)
                        val scaledW = imageW * scale
                        val scaledH = imageH * scale
                        val offsetX = (vW - scaledW) * fractionX
                        val offsetY = (vH - scaledH) * fractionY

                        val clickedIndex = detectionResult.faces.indexOfFirst { face ->
                            val rect = face.boundingBox
                            val left = (rect.left * scale) + offsetX
                            val top = (rect.top * scale) + offsetY
                            val right = left + rect.width() * scale
                            val bottom = top + rect.height() * scale

                            val touchRect = androidx.compose.ui.geometry.Rect(left, top, right, bottom).inflate(40f)
                            touchRect.contains(tapOffset)
                        }

                        if (clickedIndex != -1) {
                            onFaceClick(clickedIndex)
                        }
                    }
                }
            },
    ) {
        DivoAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = imageUri,
            contentScale = ContentScale.Crop,
            alignment = faceAlignment // Центрируем по лицу
        )

        if (detectionResult is FaceDetectionResult.Success && viewSize != IntSize.Zero && !isSearching) {
            if (detectionResult.faces.size > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .zIndex(1f)
                        .background(if (selectedFaceIndex != null) AppTheme.colors.onBackground else Color(0xFFFF9500))
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedFaceIndex != null) stringResource(R.string.ReadyToSearchFace) else stringResource(
                            R.string.MultipleFacesDetected,
                            detectionResult.faces.size
                        ),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.bodyMedium,
                        color = if (selectedFaceIndex != null) AppTheme.colors.textPrimary else AppTheme.colors.textColor
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
            ) {
                val imageWidth = detectionResult.imageWidth.toFloat()
                val imageHeight = detectionResult.imageHeight.toFloat()
                val viewWidth = size.width
                val viewHeight = size.height

                val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
                val scaledImageWidth = imageWidth * scale
                val scaledImageHeight = imageHeight * scale

                val offsetX = (viewWidth - scaledImageWidth) * fractionX
                val offsetY = (viewHeight - scaledImageHeight) * fractionY

                // Класс для хранения координат рамки
                data class FaceDrawData(
                    val sqLeft: Float, val sqTop: Float, val sideLength: Float, val rect: androidx.compose.ui.geometry.Rect
                )

                // Считаем геометрию рамок
                val faceDataList = detectionResult.faces.map { face ->
                    val rect = face.boundingBox
                    val left = (rect.left * scale) + offsetX
                    val top = (rect.top * scale) + offsetY
                    val width = rect.width() * scale
                    val height = rect.height() * scale

                    val centerX = left + width / 2f
                    val centerY = top + height / 2f
                    val sideLength = maxOf(width, height) * 0.85f
                    val verticalShift = height * 0.06f

                    val sqLeft = centerX - sideLength / 2f
                    val sqTop = (centerY - sideLength / 2f) + verticalShift

                    FaceDrawData(
                        sqLeft = sqLeft,
                        sqTop = sqTop,
                        sideLength = sideLength,
                        rect = androidx.compose.ui.geometry.Rect(sqLeft, sqTop, sqLeft + sideLength, sqTop + sideLength)
                    )
                }

                val isMultipleFaces = detectionResult.faces.size > 1
                val hasSelection = selectedFaceIndex != null

                // 1. Отрисовка затемнения фона (Только если >1 лица и есть выбор)
                if (isMultipleFaces && hasSelection && selectedFaceIndex in faceDataList.indices) {
                    val selectedRect = faceDataList[selectedFaceIndex].rect
                    val darkOverlayPath = Path().apply {
                        addRect(androidx.compose.ui.geometry.Rect(0f, 0f, viewWidth, viewHeight))
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                rect = selectedRect,
                                cornerRadius = CornerRadius(16.dp.toPx())
                            )
                        )
                        fillType = PathFillType.EvenOdd
                    }
                    drawPath(path = darkOverlayPath, color = Color.Black.copy(alpha = 0.65f))
                }

                // 2. Отрисовка рамок
                faceDataList.forEachIndexed { index, data ->
                    val isSelected = index == selectedFaceIndex

                    val cornerRadius = 16.dp.toPx()
                    val cornerLength = data.sideLength * 0.20f

                    // Функция для создания оригинального пунктирного пути
                    val buildBrokenPath = {
                        Path().apply {
                            moveTo(data.sqLeft, data.sqTop + cornerLength)
                            lineTo(data.sqLeft, data.sqTop + cornerRadius)
                            quadraticTo(data.sqLeft, data.sqTop, data.sqLeft + cornerRadius, data.sqTop)
                            lineTo(data.sqLeft + cornerLength, data.sqTop)

                            moveTo(data.sqLeft + data.sideLength - cornerLength, data.sqTop)
                            lineTo(data.sqLeft + data.sideLength - cornerRadius, data.sqTop)
                            quadraticTo(data.sqLeft + data.sideLength, data.sqTop, data.sqLeft + data.sideLength, data.sqTop + cornerRadius)
                            lineTo(data.sqLeft + data.sideLength, data.sqTop + cornerLength)

                            moveTo(data.sqLeft + data.sideLength, data.sqTop + data.sideLength - cornerLength)
                            lineTo(data.sqLeft + data.sideLength, data.sqTop + data.sideLength - cornerRadius)
                            quadraticTo(data.sqLeft + data.sideLength, data.sqTop + data.sideLength, data.sqLeft + data.sideLength - cornerRadius, data.sqTop + data.sideLength)
                            lineTo(data.sqLeft + data.sideLength - cornerLength, data.sqTop + data.sideLength)

                            moveTo(data.sqLeft + cornerLength, data.sqTop + data.sideLength)
                            lineTo(data.sqLeft + cornerRadius, data.sqTop + data.sideLength)
                            quadraticTo(data.sqLeft, data.sqTop + data.sideLength, data.sqLeft, data.sqTop + data.sideLength - cornerRadius)
                            lineTo(data.sqLeft, data.sqTop + data.sideLength - cornerLength)
                        }
                    }

                    if (isMultipleFaces && hasSelection) {
                        if (isSelected) {
                            // ВЫБРАННОЕ лицо среди нескольких: Сплошная оранжевая рамка
                            drawRoundRect(
                                color = boxColor,
                                topLeft = data.rect.topLeft,
                                size = data.rect.size,
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        } else {
                            // ПРОЧИЕ лица среди нескольких (при наличии выбора): Старая пунктирная БЕЛАЯ рамка
                            drawPath(
                                path = buildBrokenPath(),
                                color = Color.White,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    } else {
                        // ОДНО лицо ИЛИ несколько лиц без выбора (исходное состояние): Старая пунктирная ОРАНЖЕВАЯ рамка
                        drawPath(
                            path = buildBrokenPath(),
                            color = boxColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
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