package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun DivoTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit = {},
    placeholder: String = "Search",
    @DrawableRes leadingIcon: Int? = null,
    trailingIcon: Int? = null,
    onTrailingIconClick: () -> Unit = {},
    backgroundColor: Color = Color.Black.copy(alpha = 0.06f),
    cornerRadius: Dp = 10.dp,
    borderColor: Color = Color.Transparent,
    placeholderColor: Color = Color.Black.copy(alpha = 0.6f),
    trailingIconColor: Color = placeholderColor,
    cursorColor: Color = Color.Black,
    focusedBorderColor: Color = AppTheme.colors.accentOrange,
    height: Dp = 46.dp,
    maxLines: Int = 1,
    minLines: Int = 1,
    textStyle: TextStyle = TextStyle(fontSize = 17.sp),
    horizontalContentPadding: Dp = 8.dp,
    verticalContentPadding: Dp = 0.dp,
    readOnly: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = AppTheme.colors.accentOrange,
        backgroundColor = AppTheme.colors.accentOrange.copy(alpha = 0.3f)
    )

    val resolvedTextStyle = textStyle.copy(
        lineHeight = textStyle.fontSize,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both
        )
    )

    val shape = RoundedCornerShape(cornerRadius)

    val borderModifier = when {
        isFocused -> Modifier.border(1.dp, focusedBorderColor, shape)
        borderColor != Color.Transparent -> Modifier.border(1.dp, borderColor, shape)
        else -> Modifier
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(backgroundColor)
                .then(borderModifier)
                .padding(horizontal = horizontalContentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    tint = placeholderColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                maxLines = maxLines,
                minLines = minLines,
                textStyle = resolvedTextStyle,
                cursorBrush = SolidColor(cursorColor),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(vertical = verticalContentPadding),
                        contentAlignment = if (minLines == 1) Alignment.CenterStart else Alignment.TopStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = placeholderColor,
                                style = resolvedTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (trailingIcon != null) {
                if (trailingIcon == R.drawable.ic_divo_face_rec) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .offset(x = 12.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.accentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(trailingIcon),
                            contentDescription = null,
                            tint = trailingIconColor,
                            modifier = Modifier
                                .size(18.dp)
                                .clickableWithoutRipple { onTrailingIconClick() }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(trailingIcon),
                        contentDescription = null,
                        tint = trailingIconColor,
                        modifier = Modifier
                            .size(18.dp)
                            .clickableWithoutRipple { onTrailingIconClick() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Default")
@Composable
private fun DivoTextFieldDefaultPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        DivoTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "With Text & Clear")
@Composable
private fun DivoTextFieldWithTextPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        DivoTextField(
            value = "John Doe",
            onValueChange = {},
            placeholder = "Search",
            trailingIcon = R.drawable.ic_divo_close,
            onTrailingIconClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "No Leading Icon")
@Composable
private fun DivoTextFieldNoLeadingIconPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        DivoTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = null,
            placeholder = "Enter text",
            backgroundColor = Color.DarkGray
        )
    }
}