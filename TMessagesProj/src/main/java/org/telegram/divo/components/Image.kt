package org.telegram.divo.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.ui.Components.BackupImageView

@Composable
fun LocalImageView(
    filePath: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 0
) {
    if (filePath == null) return

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BackupImageView(context).apply {
                if (cornerRadius > 0) {
                    setRoundRadius(AndroidUtilities.dp(cornerRadius.toFloat()))
                }
            }
        },
        update = { view ->
            val location = ImageLocation.getForPath(filePath)
            view.setImage(location, "800_800", null as android.graphics.drawable.Drawable?, null)
        }
    )
}

@Composable
fun TelegramPhotoBackground(
    photo: String?,
    modifier: Modifier = Modifier,
    isBlurSupported: Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
    onReady: () -> Unit = {},
) {
    val mainReady = remember { mutableStateOf(false) }
    val blurReady = remember { mutableStateOf(!isBlurSupported) }

    val onReadyCallback = rememberUpdatedState(onReady)

    LaunchedEffect(mainReady.value, blurReady.value) {
        if (mainReady.value && blurReady.value) {
            onReadyCallback.value()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {

        DivoAsyncImage(
            model = photo,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
            onReady = { mainReady.value = true },
            loadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        )

        if (isBlurSupported) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()

                        drawRect(
                            brush = verticalGradient(
                                0.0f to Color.Transparent,
                                0.6f to Color.Transparent,
                                0.75f to Color.Black,
                                1.0f to Color.Black
                            ),
                            blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                        )
                    }
            ) {
                DivoAsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(35.dp),
                    alignment = Alignment.TopCenter,
                    model = photo,
                    contentScale = ContentScale.FillWidth,
                    onReady = { blurReady.value = true },
                    loadingContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppTheme.colors.backgroundLight.copy(0.6f))
                        )
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        verticalGradient(
                            0.0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            0.75f to Color.Black.copy(alpha = 0.3f),
                            1.0f to Color.Black.copy(alpha = 0.6f)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.4f),
                        0.2f to Color.Black.copy(alpha = 0.2f),
                        0.75f to Color.Transparent,
                        1.0f to Color.Transparent
                    )
                )
        )
    }
}

@Composable
fun TelegramUserAvatarEditable(
    modifier: Modifier = Modifier,
    avatarUrl: String = "",
    localUri: Uri? = null,
    size: Dp = 100.dp,
    background: Color = AppTheme.colors.backgroundDark,
    borderColor: Color = Color.White.copy(alpha = 0.4f),
    showBorder: Boolean = true,
    isVisibleSmallIcon: Boolean = true,
    placeholderSymbols: String = "",
    placeholderIconSize: Dp = 32.dp,
    usePlaceholder: Boolean = false,
    @DrawableRes smallIconResId: Int = R.drawable.ic_camera_add,
    onEditClick: () -> Unit
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(background)
                .then(
                    if (showBorder) {
                        Modifier.border(
                            width = 1.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
                .clickableWithoutRipple(onClick = onEditClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showBorder) {
                            Modifier.padding(3.dp)
                        } else {
                            Modifier.padding(1.dp)
                        }
                    )
                    .clip(CircleShape)
            ) {
                if (localUri != null || avatarUrl.isNotEmpty()) {
                    DivoAsyncImage(
                        modifier = Modifier
                            .size(size),
                        model = localUri ?: avatarUrl,
                        placeholderColor = Color.Transparent,
                        loadingContent = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LottieProgressIndicator(
                                    modifier = Modifier.size(placeholderIconSize),
                                )
                            }
                        }
                    )
                } else {
                    if (usePlaceholder) {
                        PlaceholderAvatar(
                            modifier = Modifier.fillMaxSize(),
                            name = placeholderSymbols,
                        )
                    } else {
                        Icon(
                            modifier = Modifier.align(Alignment.Center).size(placeholderIconSize),
                            painter = painterResource(R.drawable.ic_divo_add_photo),
                            contentDescription = null,
                            tint = borderColor
                        )
                    }
                }
            }

            if (showBorder) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = CircleShape
                        )
                )
            }
        }

        if (isVisibleSmallIcon) {
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape),
                painter = painterResource(smallIconResId),
                contentDescription = null,
            )
        }
    }
}
