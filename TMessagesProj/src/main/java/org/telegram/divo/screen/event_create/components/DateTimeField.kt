package org.telegram.divo.screen.event_create.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.DivoTextField
import org.telegram.divo.style.AppTheme

@Composable
fun DateTimeField(
    modifier: Modifier = Modifier,
    date: String,
    title: String,
    placeholder: String,
    trailingIcon: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(modifier) {
        SectionHeader(text = title)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            DivoTextField(
                value = date,
                horizontalContentPadding = 16.dp,
                cornerRadius = 100.dp,
                backgroundColor = AppTheme.colors.onBackground,
                textStyle = TextStyle(fontSize = 16.sp),
                readOnly = true,
                placeholder = placeholder,
                placeholderColor = AppTheme.colors.textPrimary.copy(0.4f),
                trailingIcon = trailingIcon,
                trailingIconColor = AppTheme.colors.textPrimary
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickableWithoutRipple(enabled = enabled) { onClick() }
            )
        }
    }
}