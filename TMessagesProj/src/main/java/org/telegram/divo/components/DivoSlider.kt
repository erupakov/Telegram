package org.telegram.divo.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun DivoSlider(
    modifier: Modifier = Modifier,
    value: Float,
    activeTrackColor: Color = Color(0xFF7A7A7A),
    inactiveTrackColor: Color = Color(0xFF7A7A7A),
    thumbRadius: Dp = 8.dp,
    trackHeight: Dp = 6.dp,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val thumbRadius = thumbRadius
    val trackHeight = trackHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { onValueChangeFinished?.invoke() },
                    onDragCancel = { onValueChangeFinished?.invoke() },
                ) { change, _ ->
                    change.consume()
                    val newValue = (change.position.x / sliderWidth)
                        .coerceIn(0f, 1f)
                        .let { valueRange.start + it * (valueRange.endInclusive - valueRange.start) }
                    onValueChange(newValue)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newValue = (offset.x / sliderWidth)
                        .coerceIn(0f, 1f)
                        .let { valueRange.start + it * (valueRange.endInclusive - valueRange.start) }
                    onValueChange(newValue)
                    onValueChangeFinished?.invoke()
                }
            }
    ) {
        val fraction = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY = size.height / 2
            val trackHeightPx = trackHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            val thumbX = fraction * size.width

            // Inactive track
            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(thumbX, trackY - trackHeightPx / 2),
                size = Size(size.width - thumbX, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            // Active track
            drawRoundRect(
                color = activeTrackColor,
                topLeft = Offset(0f, trackY - trackHeightPx / 2),
                size = Size(thumbX, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            // Thumb outer circle
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx,
                center = Offset(thumbX, trackY)
            )
        }
    }
}

@Composable
fun SteppedDivoSlider(
    modifier: Modifier = Modifier,
    steps: List<Int> = listOf(30, 45, 60, 75, 85, 100),
    currentStep: Int,
    activeTrackColor: Color = AppTheme.colors.accentOrange,
    inactiveTrackColor: Color = AppTheme.colors.accentOrange.copy(0.3f),
    dotColor: Color = Color.White,
    thumbRadius: Dp = 12.dp,
    trackHeight: Dp = 4.dp,
    dotRadius: Dp = 6.dp,
    tailLength: Dp = 16.dp,
    onStepChange: (Int) -> Unit,
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val segmentsCount = steps.size - 1
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(sliderWidth) {
                if (sliderWidth == 0f) return@pointerInput

                val startOffsetPx = with(density) { tailLength.toPx() }
                val endOffsetPx = sliderWidth - startOffsetPx
                val dotsWidthPx = endOffsetPx - startOffsetPx

                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = ((change.position.x - startOffsetPx) / dotsWidthPx).coerceIn(0f, 1f)
                    val nearestIndex = (fraction * segmentsCount).roundToInt()
                    onStepChange(steps[nearestIndex])
                }
            }
            .pointerInput(sliderWidth) {
                if (sliderWidth == 0f) return@pointerInput
                val startOffsetPx = with(density) { tailLength.toPx() }
                val endOffsetPx = sliderWidth - startOffsetPx
                val dotsWidthPx = endOffsetPx - startOffsetPx

                detectTapGestures { offset ->
                    val fraction = ((offset.x - startOffsetPx) / dotsWidthPx).coerceIn(0f, 1f)
                    val nearestIndex = (fraction * segmentsCount).roundToInt()
                    onStepChange(steps[nearestIndex])
                }
            }
    ) {
        val currentIndex = steps.indexOf(currentStep).coerceAtLeast(0)
        val shadowPaint = remember {
            Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(8f, 0f, 2f, android.graphics.Color.argb(80, 0, 0, 0))
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY = size.height / 2
            val trackHeightPx = trackHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            val tailLengthPx = tailLength.toPx()

            val lineStartX = 0f
            val lineEndX = size.width

            val dotsStartX = tailLengthPx
            val dotsEndX = size.width - tailLengthPx
            val dotsWidth = dotsEndX - dotsStartX

            val stepWidth = dotsWidth / segmentsCount
            val thumbX = dotsStartX + (currentIndex * stepWidth)

            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(lineStartX, trackY - trackHeightPx / 2),
                size = Size(lineEndX, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            val isLastStep = currentIndex == segmentsCount
            val activeTrackWidth = if (isLastStep) lineEndX else thumbX

            drawRoundRect(
                color = activeTrackColor,
                topLeft = Offset(lineStartX, trackY - trackHeightPx / 2),
                size = Size(activeTrackWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            for (i in 0..segmentsCount) {
                val dotX = dotsStartX + (i * stepWidth)
                val isPassed = dotX <= thumbX + 1f

                if (isPassed) {
                    drawCircle(
                        color = activeTrackColor,
                        radius = dotRadius.toPx(),
                        center = Offset(dotX, trackY)
                    )
                } else {
                    drawContext.canvas.drawCircle(
                        center = Offset(dotX, trackY),
                        radius = dotRadius.toPx(),
                        paint = shadowPaint
                    )
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius.toPx(),
                        center = Offset(dotX, trackY)
                    )
                }
            }

            drawCircle(
                color = activeTrackColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, trackY)
            )
        }
    }
}

private enum class ActiveThumb { NONE, MIN, MAX }

@Composable
fun DivoRangeSlider(
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 14f..45f,
    currentMin: Float,
    currentMax: Float,
    activeTrackColor: Color = AppTheme.colors.accentOrange,
    inactiveTrackColor: Color = Color(0xFFFFE0D4),
    thumbColor: Color = Color.White,
    thumbRadius: Dp = 12.dp,
    trackHeight: Dp = 4.dp,
    onValueChange: (min: Float, max: Float) -> Unit,
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    var activeThumb by remember { mutableStateOf(ActiveThumb.NONE) }
    val density = LocalDensity.current

    val minState by rememberUpdatedState(currentMin)
    val maxState by rememberUpdatedState(currentMax)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(sliderWidth) {
                if (sliderWidth == 0f) return@pointerInput

                val paddingPx = with(density) { thumbRadius.toPx() }
                val availableWidth = sliderWidth - 2 * paddingPx

                detectDragGestures(
                    onDragStart = { offset ->
                        val minFraction = (minState - range.start) / (range.endInclusive - range.start)
                        val maxFraction = (maxState - range.start) / (range.endInclusive - range.start)

                        val minX = paddingPx + minFraction * availableWidth
                        val maxX = paddingPx + maxFraction * availableWidth

                        val distMin = abs(offset.x - minX)
                        val distMax = abs(offset.x - maxX)
                        activeThumb = if (distMin <= distMax) ActiveThumb.MIN else ActiveThumb.MAX
                    },
                    onDragEnd = { activeThumb = ActiveThumb.NONE },
                    onDragCancel = { activeThumb = ActiveThumb.NONE }
                ) { change, _ ->
                    change.consume()
                    val fraction = ((change.position.x - paddingPx) / availableWidth).coerceIn(0f, 1f)
                    val newValue = range.start + fraction * (range.endInclusive - range.start)

                    if (activeThumb == ActiveThumb.MIN) {
                        onValueChange(min(newValue, maxState), maxState)
                    } else if (activeThumb == ActiveThumb.MAX) {
                        onValueChange(minState, max(newValue, minState))
                    }
                }
            }
            .pointerInput(sliderWidth) {
                if (sliderWidth == 0f) return@pointerInput
                val paddingPx = with(density) { thumbRadius.toPx() }
                val availableWidth = sliderWidth - 2 * paddingPx

                detectTapGestures { offset ->
                    val minFraction = (minState - range.start) / (range.endInclusive - range.start)
                    val maxFraction = (maxState - range.start) / (range.endInclusive - range.start)

                    val minX = paddingPx + minFraction * availableWidth
                    val maxX = paddingPx + maxFraction * availableWidth

                    val distMin = abs(offset.x - minX)
                    val distMax = abs(offset.x - maxX)
                    val fraction = ((offset.x - paddingPx) / availableWidth).coerceIn(0f, 1f)
                    val newValue = range.start + fraction * (range.endInclusive - range.start)

                    if (distMin <= distMax) {
                        onValueChange(min(newValue, maxState), maxState)
                    } else {
                        onValueChange(minState, max(newValue, minState))
                    }
                }
            }
    ) {
        val shadowPaint = remember {
            Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(8f, 0f, 4f, android.graphics.Color.argb(40, 0, 0, 0))
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY = size.height / 2
            val trackHeightPx = trackHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            val paddingPx = thumbRadiusPx
            val availableWidth = size.width - 2 * paddingPx

            val minFraction = (currentMin - range.start) / (range.endInclusive - range.start)
            val maxFraction = (currentMax - range.start) / (range.endInclusive - range.start)

            val minX = paddingPx + minFraction * availableWidth
            val maxX = paddingPx + maxFraction * availableWidth

            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(paddingPx, trackY - trackHeightPx / 2),
                size = Size(availableWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            drawRoundRect(
                color = activeTrackColor,
                topLeft = Offset(minX, trackY - trackHeightPx / 2),
                size = Size(maxX - minX, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            drawIntoCanvas { canvas ->
                canvas.drawCircle(Offset(minX, trackY), thumbRadiusPx, shadowPaint)
            }
            drawCircle(color = thumbColor, radius = thumbRadiusPx, center = Offset(minX, trackY))

            drawIntoCanvas { canvas ->
                canvas.drawCircle(Offset(maxX, trackY), thumbRadiusPx, shadowPaint)
            }
            drawCircle(color = thumbColor, radius = thumbRadiusPx, center = Offset(maxX, trackY))
        }
    }
}