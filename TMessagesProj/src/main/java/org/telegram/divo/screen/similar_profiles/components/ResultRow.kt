package org.telegram.divo.screen.similar_profiles.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.style.AppTheme

@Composable
fun ResultRow(
    imageUrl: String,
    result: String,
    alignment: Alignment,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DivoAsyncImage(
            modifier = Modifier.size(52.dp).clip(CircleShape),
            model = imageUrl,
            alignment = alignment
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = result,
            fontSize = 20.sp,
            style = AppTheme.typography.helveticaNeueLtCom,
            color = AppTheme.colors.textPrimary
        )
    }
}