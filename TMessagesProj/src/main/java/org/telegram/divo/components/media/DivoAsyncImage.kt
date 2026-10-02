package org.telegram.divo.components.media

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import okhttp3.OkHttpClient
import org.telegram.divo.common.compose.shimmer
import org.telegram.divo.style.AppTheme
import java.util.concurrent.TimeUnit

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
    onError: () -> Unit = {},
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
        onError = onError,
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
    onError: () -> Unit = {},
    loadingContent: (@Composable () -> Unit)?,
    errorContent: (@Composable () -> Unit)?,
) {
    val context = LocalContext.current
    val imageLoader = CoilSingleton.getImageLoader(context)

    Box(modifier = modifier) {
        SubcomposeAsyncImage(
            model = model,
            imageLoader = imageLoader,
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
            error = { errorState ->
                LaunchedEffect(Unit) {
                    onError()
                    onReady()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = alignment,
                ) {
                    errorContent?.invoke() ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.onBackground)
                            .shimmer()
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

private object CoilSingleton {
    @Volatile
    private var instance: ImageLoader? = null

    fun getImageLoader(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .okHttpClient {
                    OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                                .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                                .build()
                            chain.proceed(request)
                        }
                        .build()
                }
                .build().also { instance = it }
        }
    }
}