package org.telegram.divo.screen.reg_form.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme

@Composable
fun TextHint(
    text: String
) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 6.dp),
        text = text,
        style = AppTheme.typography.helveticaNeueRegular,
        lineHeight = 14.sp,
        color = AppTheme.colors.textPrimary.copy(0.6f),
        fontSize = 12.sp
    )
}