package org.telegram.divo.screen.your_parameters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.style.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSelector(
    modifier: Modifier = Modifier,
    label: String,
    items: List<String>,
    selectedValue: String,
    onItemSelected: (Int, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedValue.ifEmpty { label }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        DivoTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = selectedText,
            placeholder = label,
            trailingIcon = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            backgroundColor = Color.White.copy(0.05f),
            borderColor = Color.White.copy(0.40f),
            cornerRadius = 10.dp,
            placeholderColor = Color.White.copy(0.50f),
            cursorColor = Color.White,
            textStyle = AppTheme.typography.helveticaNeueRegular.copy(
                fontSize = 16.sp,
                color = Color.White
            ),
            readOnly = true,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 13.dp),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.Transparent,
            modifier = Modifier
                .background(
                    color = Color(0xFF2A2A2A),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color.White) },
                    onClick = {
                        onItemSelected(index + 1, item)
                        expanded = false
                    }
                )
            }
        }
    }
}