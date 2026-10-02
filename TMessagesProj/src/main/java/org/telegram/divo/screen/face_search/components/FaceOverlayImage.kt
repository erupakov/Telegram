package org.telegram.divo.screen.face_search.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import org.telegram.divo.components.media.DivoAsyncImage
import org.telegram.divo.common.compose.shimmer
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

    var fractionX by remember { mutableFloatStateOf(0.5f) }
    var fractionY by remember { mutableFloatStateOf(0.5f) }
    var showDragHint by remember { mutableStateOf(false) }

    LaunchedEffect(detectionResult, viewSize) {
        if (detectionResult is FaceDetectionResult.Success && detectionResult.faces.size > 1 && viewSize != IntSize.Zero) {
            val mainFace = detectionResult.faces.first()
            val imageW = detectionResult.imageWidth.toFloat()
            val imageH = detectionResult.imageHeight.toFloat()
            val initFractionX = (mainFace.centerX / imageW).coerceIn(0f, 1f)
            val initFractionY = (mainFace.centerY / imageH).coerceIn(0f, 1f)

            val vW = viewSize.width.toFloat()
            val vH = viewSize.height.toFloat()

            val scale = maxOf(vW / imageW, vH / imageH)
            val scaledW = imageW * scale
            val scaledH = imageH * scale
            
            var offsetX = vW / 2f - initFractionX * scaledW
            var offsetY = vH / 2f - initFractionY * scaledH
            
            val minOffsetX = vW - scaledW
            val minOffsetY = vH - scaledH
            
            offsetX = offsetX.coerceIn(minOffsetX, 0f)
            offsetY = offsetY.coerceIn(minOffsetY, 0f)

            val hasFaceOutside = detectionResult.faces.any { face ->
                val left = (face.x1 * scale) + offsetX
                val top = (face.y1 * scale) + offsetY
                val right = (face.x2 * scale) + offsetX
                val bottom = (face.y2 * scale) + offsetY
                
                left < 0f || top < 0f || right > vW || bottom > vH
            }

            if (hasFaceOutside) {
                delay(1000)
                showDragHint = true
                delay(4500)
                showDragHint = false
            }
        }
    }

    LaunchedEffect(detectionResult) {
        if (detectionResult is FaceDetectionResult.Success && detectionResult.faces.isNotEmpty()) {
            val mainFace = detectionResult.faces.first()
            val imageW = detectionResult.imageWidth.toFloat()
            val imageH = detectionResult.imageHeight.toFloat()
            fractionX = (mainFace.centerX / imageW).coerceIn(0f, 1f)
            fractionY = (mainFace.centerY / imageH).coerceIn(0f, 1f)
        } else {
            fractionX = 0.5f
            fractionY = 0.5f
        }
    }

    val bottomOverlayHeightDp = remember(detectionResult, isSearching) {
        if (detectionResult is FaceDetectionResult.Success && detectionResult.faces.size > 1 && !isSearching) {
            48.dp
        } else {
            0.dp
        }
    }

    val faceAlignment = remember(fractionX, fractionY) {
        object : Alignment {
            override fun align(
                size: IntSize,
                space: IntSize,
                layoutDirection: androidx.compose.ui.unit.LayoutDirection
            ): androidx.compose.ui.unit.IntOffset {
                var offsetX = space.width / 2f - fractionX * size.width.toFloat()
                var offsetY = space.height / 2f - fractionY * size.height.toFloat()

                val minOffsetX = space.width.toFloat() - size.width.toFloat()
                val minOffsetY = space.height.toFloat() - size.height.toFloat()

                offsetX = offsetX.coerceIn(minOffsetX, 0f)
                offsetY = offsetY.coerceIn(minOffsetY, 0f)

                return androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt())
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
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(32.dp))
                .aspectRatio(1f)
                .background(AppTheme.colors.onBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomOverlayHeightDp)
                    .onSizeChanged { viewSize = it }
                    .alpha(if (detectionResult is FaceDetectionResult.Loading) 0f else 1f)
                    .pointerInput(detectionResult, viewSize, isSearching) {
                        detectTapGestures { tapOffset ->
                            if (detectionResult is FaceDetectionResult.Success && !isSearching && viewSize != IntSize.Zero && detectionResult.faces.size > 1) {
                                if (showDragHint) showDragHint = false
                                val imageW = detectionResult.imageWidth.toFloat()
                                val imageH = detectionResult.imageHeight.toFloat()
                                val vW = viewSize.width.toFloat()
                                val vH = viewSize.height.toFloat()

                                val scale = maxOf(vW / imageW, vH / imageH)
                                val scaledW = imageW * scale
                                val scaledH = imageH * scale
                                
                                var offsetX = vW / 2f - fractionX * scaledW
                                var offsetY = vH / 2f - fractionY * scaledH
                                
                                val minOffsetX = vW - scaledW
                                val minOffsetY = vH - scaledH
                                
                                offsetX = offsetX.coerceIn(minOffsetX, 0f)
                                offsetY = offsetY.coerceIn(minOffsetY, 0f)

                                val clickedIndex = detectionResult.faces.indexOfFirst { face ->
                                    val left = (face.x1 * scale) + offsetX
                                    val top = (face.y1 * scale) + offsetY
                                    val right = (face.x2 * scale) + offsetX
                                    val bottom = (face.y2 * scale) + offsetY

                                    val touchRect =
                                        androidx.compose.ui.geometry.Rect(left, top, right, bottom)
                                            .inflate(40f)
                                    touchRect.contains(tapOffset)
                                }

                                if (clickedIndex != -1) {
                                    onFaceClick(clickedIndex)
                                }
                            }
                        }
                    }
                    .pointerInput(detectionResult, viewSize) {
                        detectDragGestures { change, dragAmount ->
                            if (viewSize != IntSize.Zero && detectionResult is FaceDetectionResult.Success) {
                                if (showDragHint) showDragHint = false
                                change.consume()
                                val imageW = detectionResult.imageWidth.toFloat()
                                val imageH = detectionResult.imageHeight.toFloat()
                                val vW = viewSize.width.toFloat()
                                val vH = viewSize.height.toFloat()

                                val scale = maxOf(vW / imageW, vH / imageH)
                                val scaledW = imageW * scale
                                val scaledH = imageH * scale

                                var minFracX = (vW / 2f) / scaledW
                                var maxFracX = 1f - (vW / 2f) / scaledW
                                if (minFracX > maxFracX) {
                                    val temp = minFracX
                                    minFracX = maxFracX
                                    maxFracX = temp
                                }

                                var minFracY = (vH / 2f) / scaledH
                                var maxFracY = 1f - (vH / 2f) / scaledH
                                if (minFracY > maxFracY) {
                                    val temp = minFracY
                                    minFracY = maxFracY
                                    maxFracY = temp
                                }

                                val newFracX = (fractionX - dragAmount.x / scaledW).coerceIn(minFracX, maxFracX)
                                val newFracY = (fractionY - dragAmount.y / scaledH).coerceIn(minFracY, maxFracY)
                                
                                fractionX = newFracX
                                fractionY = newFracY
                            }
                        }
                    }
            ) {
                DivoAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = imageUri,
                    contentScale = ContentScale.Crop,
                    alignment = faceAlignment
                )

                if (detectionResult is FaceDetectionResult.Success && viewSize != IntSize.Zero && !isSearching) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val imageWidth = detectionResult.imageWidth.toFloat()
                        val imageHeight = detectionResult.imageHeight.toFloat()
                        val viewWidth = size.width
                        val viewHeight = size.height

                        val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
                        val scaledImageWidth = imageWidth * scale
                        val scaledImageHeight = imageHeight * scale

                        var offsetX = viewWidth / 2f - fractionX * scaledImageWidth
                        var offsetY = viewHeight / 2f - fractionY * scaledImageHeight

                        val minOffsetX = viewWidth - scaledImageWidth
                        val minOffsetY = viewHeight - scaledImageHeight

                        offsetX = offsetX.coerceIn(minOffsetX, 0f)
                        offsetY = offsetY.coerceIn(minOffsetY, 0f)

                        data class FaceDrawData(
                            val sqLeft: Float,
                            val sqTop: Float,
                            val sideLength: Float,
                            val rect: androidx.compose.ui.geometry.Rect
                        )

                        val faceDataList = detectionResult.faces.map { face ->
                            val left = (face.x1 * scale) + offsetX
                            val top = (face.y1 * scale) + offsetY
                            val width = face.width * scale
                            val height = face.height * scale

                            val centerX = left + width / 2f
                            val centerY = top + height / 2f
                            val sideLength = maxOf(width, height)
                            val verticalShift = 0f

                            val sqLeft = centerX - sideLength / 2f
                            val sqTop = (centerY - sideLength / 2f) + verticalShift

                            FaceDrawData(
                                sqLeft = sqLeft,
                                sqTop = sqTop,
                                sideLength = sideLength,
                                rect = androidx.compose.ui.geometry.Rect(
                                    sqLeft,
                                    sqTop,
                                    sqLeft + sideLength,
                                    sqTop + sideLength
                                )
                            )
                        }

                        val isMultipleFaces = detectionResult.faces.size > 1
                        val hasSelection = selectedFaceIndex != null

                        if (isMultipleFaces && hasSelection && selectedFaceIndex in faceDataList.indices) {
                            val selectedData = faceDataList[selectedFaceIndex]
                            val selectedRect = selectedData.rect
                            val cornerLengthOverlay = selectedData.sideLength * 0.25f
                            val cornerRadiusOverlay = minOf(16.dp.toPx(), cornerLengthOverlay * 0.8f)

                            val darkOverlayPath = Path().apply {
                                addRect(
                                    androidx.compose.ui.geometry.Rect(
                                        0f,
                                        0f,
                                        viewWidth,
                                        viewHeight
                                    )
                                )
                                addRoundRect(
                                    androidx.compose.ui.geometry.RoundRect(
                                        rect = selectedRect,
                                        cornerRadius = CornerRadius(cornerRadiusOverlay)
                                    )
                                )
                                fillType = PathFillType.EvenOdd
                            }
                            drawPath(path = darkOverlayPath, color = Color.Black.copy(alpha = 0.65f))
                        }

                        faceDataList.forEachIndexed { index, data ->
                            val isSelected = index == selectedFaceIndex

                            val cornerLength = data.sideLength * 0.25f
                            val cornerRadius = minOf(16.dp.toPx(), cornerLength * 0.8f)

                            val buildBrokenPath = {
                                Path().apply {
                                    moveTo(data.sqLeft, data.sqTop + cornerLength)
                                    lineTo(data.sqLeft, data.sqTop + cornerRadius)
                                    quadraticTo(
                                        data.sqLeft,
                                        data.sqTop,
                                        data.sqLeft + cornerRadius,
                                        data.sqTop
                                    )
                                    lineTo(data.sqLeft + cornerLength, data.sqTop)

                                    moveTo(data.sqLeft + data.sideLength - cornerLength, data.sqTop)
                                    lineTo(data.sqLeft + data.sideLength - cornerRadius, data.sqTop)
                                    quadraticTo(
                                        data.sqLeft + data.sideLength,
                                        data.sqTop,
                                        data.sqLeft + data.sideLength,
                                        data.sqTop + cornerRadius
                                    )
                                    lineTo(data.sqLeft + data.sideLength, data.sqTop + cornerLength)

                                    moveTo(
                                        data.sqLeft + data.sideLength,
                                        data.sqTop + data.sideLength - cornerLength
                                    )
                                    lineTo(
                                        data.sqLeft + data.sideLength,
                                        data.sqTop + data.sideLength - cornerRadius
                                    )
                                    quadraticTo(
                                        data.sqLeft + data.sideLength,
                                        data.sqTop + data.sideLength,
                                        data.sqLeft + data.sideLength - cornerRadius,
                                        data.sqTop + data.sideLength
                                    )
                                    lineTo(
                                        data.sqLeft + data.sideLength - cornerLength,
                                        data.sqTop + data.sideLength
                                    )

                                    moveTo(data.sqLeft + cornerLength, data.sqTop + data.sideLength)
                                    lineTo(data.sqLeft + cornerRadius, data.sqTop + data.sideLength)
                                    quadraticTo(
                                        data.sqLeft,
                                        data.sqTop + data.sideLength,
                                        data.sqLeft,
                                        data.sqTop + data.sideLength - cornerRadius
                                    )
                                    lineTo(data.sqLeft, data.sqTop + data.sideLength - cornerLength)
                                }
                            }

                            if (isMultipleFaces && hasSelection) {
                                if (isSelected) {
                                    drawRoundRect(
                                        color = boxColor,
                                        topLeft = data.rect.topLeft,
                                        size = data.rect.size,
                                        cornerRadius = CornerRadius(cornerRadius),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                } else {
                                    drawPath(
                                        path = buildBrokenPath(),
                                        color = Color.White,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            } else {
                                drawPath(
                                    path = buildBrokenPath(),
                                    color = boxColor,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showDragHint && !isSearching,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .zIndex(2f)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.DragToMovePhoto),
                                style = AppTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            } // END OF INNER BOX

            // OVERLAY DRAWN ON OUTER BOX
            if (detectionResult is FaceDetectionResult.Success && viewSize != IntSize.Zero && !isSearching) {
                if (detectionResult.faces.size > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .zIndex(1f)
                            .background(
                                if (selectedFaceIndex != null) AppTheme.colors.onBackground else Color(
                                    0xFFFF9500
                                )
                            )
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedFaceIndex != null) stringResource(R.string.ReadyToSearchFace) else pluralStringResource(
                                R.plurals.MultipleFacesDetected,
                                detectionResult.faces.size,
                                detectionResult.faces.size
                            ),
                            textAlign = TextAlign.Center,
                            style = AppTheme.typography.bodyMedium,
                            color = if (selectedFaceIndex != null) AppTheme.colors.textPrimary else AppTheme.colors.textColor
                        )
                    }
                }
            }

            if (isSearching) {
                val infiniteTransition = rememberInfiniteTransition(label = "scanner_transition")
                val scanFraction by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "scanner_animation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val glowHeight = 60.dp.toPx() 

                    val lineY = scanFraction * (canvasHeight + glowHeight)

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

                    drawLine(
                        color = Color(0xFFFF6B00),
                        start = Offset(x = 0f, y = lineY),
                        end = Offset(x = canvasWidth, y = lineY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}