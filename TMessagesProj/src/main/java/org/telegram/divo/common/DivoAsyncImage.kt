package org.telegram.divo.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import org.telegram.divo.style.AppTheme

@Composable
fun DivoAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    crossfadeDurationMs: Int = 300,
    placeholderColor: Color = AppTheme.colors.accentColor,
    errorIconSize: Dp = 32.dp,
    loadingContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable () -> Unit)? = null,
) {
    ImageCore(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        crossfadeDurationMs = crossfadeDurationMs,
        placeholderColor = placeholderColor,
        errorIconSize = errorIconSize,
        loadingContent = loadingContent,
        errorContent = errorContent,
    )
}

@Composable
private fun ImageCore(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    crossfadeDurationMs: Int,
    placeholderColor: Color,
    errorIconSize: Dp,
    loadingContent: (@Composable () -> Unit)?,
    errorContent: (@Composable () -> Unit)?,
) {
    val context = LocalContext.current
    var painterState by remember {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    val isSuccess = painterState is AsyncImagePainter.State.Success
    val isError   = painterState is AsyncImagePainter.State.Error

    val imageRequest = remember(model, crossfadeDurationMs) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(crossfadeDurationMs)
            .build()
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize(),
            onState = { painterState = it },
        )

        AnimatedVisibility(
            visible = !isSuccess,
            exit = fadeOut(animationSpec = tween(durationMillis = crossfadeDurationMs)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderColor),
                contentAlignment = Alignment.Center,
            ) {
                if (isError) {
                    errorContent?.invoke() ?: Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Failed to load image",
                        modifier = Modifier.size(errorIconSize),
                        tint = AppTheme.colors.accentColor.copy(alpha = 0.4f),
                    )
                } else {
                    loadingContent?.invoke() ?: CircularProgressIndicator(
                        modifier = Modifier.size(errorIconSize),
                        strokeWidth = 2.dp,
                        color = AppTheme.colors.accentColor.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}