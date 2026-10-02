package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme

@Composable
fun ContainerItems(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground)
            .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            content()
        }
    }
}