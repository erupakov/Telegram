package org.telegram.divo.screen.event_create.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.telegram.divo.style.AppTheme

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary.copy(0.6f)
        )
    }
}