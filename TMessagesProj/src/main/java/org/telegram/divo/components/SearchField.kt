package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun SearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search",
    @DrawableRes leadingIcon: Int? = R.drawable.ic_divo_search,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    label: String? = null,
    backgroundColor: Color = Color.Black.copy(alpha = 0.06f),
    cornerRadius: Dp = 10.dp,
) {
    Column(modifier = modifier) {
        // Лейбл снаружи
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
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 17.sp,
                color = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading icon
                    if (leadingIcon != null) {
                        Icon(
                            painter = painterResource(leadingIcon),
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Input + Placeholder
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color.Black.copy(alpha = 0.6f),
                                fontSize = 17.sp
                            )
                        }
                        innerTextField()
                    }

                    // Trailing icon
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickableWithoutRipple { onTrailingIconClick() }
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Default")
@Composable
private fun SearchFieldDefaultPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "With Label")
@Composable
private fun SearchFieldWithLabelPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        SearchField(
            value = query,
            onValueChange = { query = it },
            label = "Filter by name",
            placeholder = "Enter name"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "With Text & Clear")
@Composable
private fun SearchFieldWithTextPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SearchField(
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
private fun SearchFieldNoLeadingIconPreview() {
    var query by remember { mutableStateOf("") }
    Box(modifier = Modifier.padding(16.dp)) {
        SearchField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = null,
            placeholder = "Enter text",
            backgroundColor = Color.DarkGray
        )
    }
}