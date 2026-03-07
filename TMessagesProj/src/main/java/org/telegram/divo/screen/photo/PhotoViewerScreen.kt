package org.telegram.divo.screen.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.components.BackButton
import org.telegram.divo.components.LottieProgressIndicator

@Composable
fun PhotoViewerScreen(
    url: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        DivoAsyncImage(
            modifier = Modifier.fillMaxSize(),
            url = url,
            contentScale = ContentScale.Fit,
            loadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(
                        modifier = Modifier.size(34.dp),
                        color = Color.White
                    )
                }
            }
        )

        BackButton(
            modifier = Modifier
                .padding(start = 16.dp, top = 44.dp)
                .align(Alignment.TopStart),
            onBackClicked = onBack
        )
    }
}
