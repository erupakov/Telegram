package org.telegram.divo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont.HelveticaNeue


private val Copper = Color(0xFFC57B53)
private val FieldBg = Color(0xFF2B2B2B)
private val FieldBorder = Color(0xFF4A4A4A)
private val FieldHint = Color(0x99FFFFFF)

@Composable
fun DivoTextFieldCard(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 16.sp),
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val shape = RoundedCornerShape(12.dp)
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(46.dp)                                  // ← exact height
            .clip(shape)
            .background(FieldBg)
            .border(1.dp, FieldBorder, shape)
            .then(if (readOnly && onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp)                    // horizontal padding
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = { if (!readOnly) onValueChange(it) },
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(Color.White),
                    visualTransformation = visualTransformation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused }
                )
                if (value.isEmpty()) {
                    Text(placeholder, color = FieldHint, fontSize = 16.sp)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}


@Composable
fun UiDarkTextField(
    modifier: Modifier = Modifier,
    value: String = "value",
    onValueChange: (String) -> Unit = {},
    label: String = "Name event",
    minLines: Int = 1,
    singleLine:Boolean = true
    ) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            UITextHint(
                text = label,
                color = AppTheme.colors.textLabelDark
            )
        },
        minLines = minLines,
        textStyle = TextStyle.Default.copy(fontFamily = HelveticaNeue),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = AppTheme.colors.textColor,
            focusedBorderColor = AppTheme.colors.textColor,
            unfocusedBorderColor = AppTheme.colors.textLabelDark,
            ),
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words
        )
    )
}


@Preview
@Composable
private fun PreviewDark() {
    Scaffold(
        containerColor = AppTheme.colors.backgroundDark
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            UiDarkTextField()
        }
    }
}
