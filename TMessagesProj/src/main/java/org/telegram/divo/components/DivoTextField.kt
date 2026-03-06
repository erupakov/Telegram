package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.messenger.R

@Composable
fun DivoTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search",
    @DrawableRes leadingIcon: Int? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    label: String? = null,
    backgroundColor: Color = Color.Black.copy(alpha = 0.06f),
    cornerRadius: Dp = 10.dp,
    borderColor: Color = Color.Transparent,
    placeholderColor: Color = Color.Black.copy(alpha = 0.6f),
    cursorColor: Color = Color.Black,
    minLines: Int = 1,
    maxLines: Int = 1,
    textStyle: TextStyle = TextStyle(fontSize = 17.sp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
    innerLabel: String? = null,
    innerLabelColor: Color = Color.Black.copy(alpha = 0.5f),
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = textStyle,
            cursorBrush = SolidColor(cursorColor),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
                    } else Modifier
                )
                .padding(contentPadding),
            decorationBox = { innerTextField ->
                Column {
                    if (innerLabel != null) {
                        Text(
                            text = innerLabel,
                            color = innerLabelColor,
                            style = textStyle.copy(fontSize = 13.sp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(verticalAlignment = if (maxLines == 1) Alignment.CenterVertically else Alignment.Top) {
                        if (leadingIcon != null) {
                            Icon(
                                painter = painterResource(leadingIcon),
                                contentDescription = null,
                                tint = placeholderColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(if (maxLines > 1) Modifier.padding(top = 2.dp) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = placeholderColor,
                                    style = textStyle,
                                )
                            }
                            innerTextField()
                        }

                        if (trailingIcon != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                tint = placeholderColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickableWithoutRipple { onTrailingIconClick() }
                            )
                        }
                    }
                }
            }
        )
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "With Label")
@Composable
private fun DivoTextFieldWithLabelPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        DivoTextField(
            value = query,
            onValueChange = { query = it },
            label = "Filter by name",
            placeholder = "Enter name"
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
            trailingIcon = Icons.Default.Close,
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