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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun DivoSlider(
    modifier: Modifier = Modifier,
    value: Float,
    activeTrackColor: Color = Color(0xFF7A7A7A),
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val thumbRadius = 8.dp
    val trackHeight = 6.dp

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
                color = Color(0xFF7A7A7A),
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
                color = Color(0xFFBF7A54),
                radius = thumbRadiusPx,
                center = Offset(thumbX, trackY)
            )

            // Thumb inner circle
            drawCircle(
                color = Color(0xFFD9D9D9),
                radius = thumbRadiusPx - 2.dp.toPx(),
                center = Offset(thumbX, trackY)
            )
        }
    }
}