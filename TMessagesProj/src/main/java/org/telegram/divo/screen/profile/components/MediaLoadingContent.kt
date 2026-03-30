package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.shimmer
import org.telegram.divo.style.AppTheme

@Composable
fun MediaLoadingContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(4) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(0.5.dp)
                            .background(AppTheme.colors.backgroundLight)
                            .shimmer()
                    )
                }
            }
        }
    }
}