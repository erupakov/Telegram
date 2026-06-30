package org.telegram.divo.components.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.telegram.divo.components.inputs.RoundedGlassButton
import org.telegram.divo.style.AppTheme

@Composable
fun TransparentToolBarBackground(
    modifier: Modifier = Modifier,
    transitionProgress: Float,
    hazeState: HazeState? = null,
) {
    val rawTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var statusBarPadding by remember { mutableStateOf(rawTopPadding) }
    if (rawTopPadding > statusBarPadding) {
        statusBarPadding = rawTopPadding
    }
    val density = LocalDensity.current
    val extendedHeight = 24.dp
    val toolbarHeight = statusBarPadding + 56.dp
    val totalHeight = toolbarHeight + extendedHeight

    val startYPx = with(density) { (statusBarPadding + 46.dp).toPx() }
    val endYPx = with(density) { totalHeight.toPx() }

    if (hazeState != null && transitionProgress > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(totalHeight)
                .graphicsLayer { alpha = transitionProgress }
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = AppTheme.colors.backgroundLight,
                        blurRadius = 40.dp,
                        tints = listOf(
                            HazeTint(AppTheme.colors.backgroundLight.copy(alpha = 0.75f))
                        )
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startY = startYPx,
                        startIntensity = 1f,
                        endY = endYPx,
                        endIntensity = 0f,
                        easing = LinearEasing
                    )
                }
        )
    }
}

@Composable
fun TransparentToolBarContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    transitionProgress: Float = 0f,
    isSolid: Boolean = false,
    alwaysShowTitle: Boolean = false,
    titleContent: @Composable (BoxScope.(progress: Float) -> Unit)? = null,
    actionsContent: @Composable (BoxScope.(progress: Float, buttonBgColor: Color, buttonIconColor: Color, buttonBorderColor: Color) -> Unit)? = null,
) {
    val buttonBgColor = lerp(AppTheme.colors.onBackground.copy(alpha = 0.2f), Color.White, transitionProgress)
    val buttonIconColor = lerp(Color.White, Color.Black, transitionProgress)
    val buttonBorderColor = lerp(AppTheme.colors.onBackground.copy(alpha = 0.4f), Color.Transparent, transitionProgress)

    val rawTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var statusBarPadding by remember { mutableStateOf(rawTopPadding) }
    if (rawTopPadding > statusBarPadding) {
        statusBarPadding = rawTopPadding
    }

    val animatable = remember { Animatable(0f) }
    LaunchedEffect(isSolid) {
        animatable.animateTo(
            targetValue = if (isSolid) 1f else 0f,
            animationSpec = tween(if (isSolid) 200 else 100)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding + 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val boxScope = this
        RoundedGlassButton(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart),
            background = buttonBgColor,
            iconTint = buttonIconColor,
            borderColor = buttonBorderColor,
            onClick = onNavigateBack
        )

        if (titleContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp)
                    .graphicsLayer {
                        val progress = animatable.value
                        alpha = if (alwaysShowTitle) 1f else progress
                        translationY = if (alwaysShowTitle) 0f else (1f - progress) * 15f
                    },
                contentAlignment = Alignment.Center
            ) {
                boxScope.titleContent(animatable.value)
            }
        }

        if (actionsContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                actionsContent(transitionProgress, buttonBgColor, buttonIconColor, buttonBorderColor)
            }
        }
    }
}
