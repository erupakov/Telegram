package org.telegram.divo.screen.event_list.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.DivoAsyncImage

@Composable
fun EventItemBackground(
    url: String?,
    hazeState: HazeState,
) {
    var componentHeight by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { componentHeight = it.height.toFloat() }
    ) {
        DivoAsyncImage(
            model = url,
            modifier = Modifier
                .matchParentSize()
                .hazeSource(state = hazeState)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.Black,
                        blurRadius = 30.dp,
                        tints = listOf(HazeTint(Color.Black.copy(alpha = 0.2f)))
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startY = componentHeight * 0.6f,
                        startIntensity = 0f,
                        endY = componentHeight * 0.8f,
                        endIntensity = 1f,
                        easing = LinearEasing
                    )
                }
        )
    }
}