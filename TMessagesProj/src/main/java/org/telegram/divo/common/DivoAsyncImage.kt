package org.telegram.divo.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import org.telegram.divo.components.shimmer
import org.telegram.divo.style.AppTheme

@Composable
fun DivoAsyncImage(
    modifier: Modifier = Modifier,
    model: Any?,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    placeholderColor: Color = Color.White,
    errorIconSize: Dp = 32.dp,
    onReady: () -> Unit = {},
    loadingContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable () -> Unit)? = null,
) {
    ImageCore(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        alpha = alpha,
        colorFilter = colorFilter,
        placeholderColor = placeholderColor,
        errorIconSize = errorIconSize,
        onReady = onReady,
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
    alignment: Alignment,
    alpha: Float,
    colorFilter: ColorFilter?,
    placeholderColor: Color,
    errorIconSize: Dp,
    onReady: () -> Unit = {},
    loadingContent: (@Composable () -> Unit)?,
    errorContent: (@Composable () -> Unit)?,
) {
    Box(modifier = modifier) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = alignment,
                ) {
                    loadingContent?.invoke() ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer(),
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = alignment,
                ) {
                    errorContent?.invoke() ?: Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(errorIconSize),
                        tint = AppTheme.colors.accentOrange.copy(alpha = 0.4f),
                    )
                }
            },
            success = {
                LaunchedEffect(Unit) {
                    onReady()
                }
                SubcomposeAsyncImageContent()
            }
        )
    }
}