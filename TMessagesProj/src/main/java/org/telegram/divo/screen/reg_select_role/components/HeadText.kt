package org.telegram.divo.screen.reg_select_role.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme

@Composable
fun HeadText(
    title: String,
    subTitle: String
) {
    Column(
        modifier = Modifier
    ) {
        Text(
            text = title.uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = subTitle,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary.copy(0.8f)
        )
    }
}