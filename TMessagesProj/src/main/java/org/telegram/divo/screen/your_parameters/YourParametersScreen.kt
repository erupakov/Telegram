package org.telegram.divo.screen.your_parameters

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun YourParametersScreen(
    viewModel: YourParametersViewModel = viewModel(),
    showTitle: Boolean = true,
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setIntent(YourParametersViewModel.YourParametersIntent.OnLoad)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is YourParametersViewModel.YourParametersEffect.SaveSuccess -> {
                    Toast.makeText(context, "Parameters saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
                is YourParametersViewModel.YourParametersEffect.NavigateBack -> {
                    onBack()
                }
                is YourParametersViewModel.YourParametersEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222222)).padding(top = 32.dp)
        ) {
            val headerHeight = 44.dp
            val bottomBarHeight = 74.dp

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFBF7A54)
                )
            }
            if (showTitle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Your parameters".uppercase(),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = headerHeight + 8.dp, bottom = bottomBarHeight + 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ParameterSelector(
                    label = "Select a Gender",
                    items = listOf("Male", "Female"),
                    selectedValue = state.gender,
                    onItemSelected = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnGenderChanged(
                                it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                ParameterSlider(
                    label = "Age (y.o)",
                    range = 14f..45f,
                    value = state.age,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnAgeChanged(
                                it
                            )
                        )
                    },
                    minLabel = "14",
                    maxLabel = "45",
                    valueFormatter = { v -> "${v.toInt()} y.o" }
                )
                ParameterSlider(
                    label = "Height (cm)",
                    range = 140f..220f,
                    value = state.height,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnHeightChanged(
                                it
                            )
                        )
                    },
                    minLabel = "140",
                    maxLabel = "220",
                    valueFormatter = { v -> "${v.toInt()} cm" }
                )
                ParameterSlider(
                    label = "Waist (cm)",
                    range = 48f..90f,
                    value = state.waist,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnWaistChanged(
                                it
                            )
                        )
                    },
                    minLabel = "48",
                    maxLabel = "90",
                    valueFormatter = { v -> "${v.toInt()} cm" }
                )
                ParameterSlider(
                    label = "Hips (cm)",
                    range = 80f..110f,
                    value = state.hips,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnHipsChanged(
                                it
                            )
                        )
                    },
                    minLabel = "80",
                    maxLabel = "110",
                    valueFormatter = { v -> "${v.toInt()} cm" }
                )
                ParameterSlider(
                    label = "Shoe size (EU)",
                    range = 36f..42f,
                    value = state.shoeSize,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnShoeSizeChanged(
                                it
                            )
                        )
                    },
                    minLabel = "36",
                    maxLabel = "42",
                    valueFormatter = { v -> v.toInt().toString() }
                )
                ParameterSlider(
                    label = "Hair length (cm)",
                    range = 0f..200f,
                    value = state.hairLength,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnHairLengthChanged(
                                it
                            )
                        )
                    },
                    minLabel = "0",
                    maxLabel = "200",
                    valueFormatter = { v -> v.toInt().toString() }
                )
                Spacer(modifier = Modifier.height(24.dp))
                ParameterSelector(
                    label = "Choose your hair color",
                    items = listOf("Black", "Brown", "Blonde", "Red"),
                    selectedValue = state.hairColor,
                    onItemSelected = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnHairColorChanged(
                                it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ParameterSelector(
                    label = "Choose your eye color",
                    items = listOf("Brown", "Blue", "Green", "Hazel"),
                    selectedValue = state.eyeColor,
                    onItemSelected = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnEyeColorChanged(
                                it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ParameterSelector(
                    label = "Choose your skin color",
                    items = listOf("Light", "Fair", "Medium", "Dark"),
                    selectedValue = state.skinColor,
                    onItemSelected = {
                        viewModel.setIntent(
                            YourParametersViewModel.YourParametersIntent.OnSkinColorChanged(
                                it
                            )
                        )
                    }
                )
                if (state.gender == "Female") {
                    Spacer(modifier = Modifier.height(16.dp))
                    ParameterSelector(
                        label = "Choose your breast size",
                        items = listOf("A", "B", "C", "D", "DD", "E", "F"),
                        selectedValue = state.breastSize,
                        onItemSelected = {
                            viewModel.setIntent(
                                YourParametersViewModel.YourParametersIntent.OnBreastSizeChanged(
                                    it
                                )
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Fixed bottom buttons (not scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.setIntent(YourParametersViewModel.YourParametersIntent.OnBackClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3A)),
                    enabled = !state.isLoading
                ) {
                    Text(text = "← Back", color = Color.White, fontSize = 18.sp)
                }
                Button(
                    onClick = { viewModel.setIntent(YourParametersViewModel.YourParametersIntent.OnSaveClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF7A54)),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Save", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSelector(
    label: String,
    items: List<String>,
    selectedValue: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = if (selectedValue.isEmpty()) label else selectedValue

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White.copy(alpha = 0.6f),
                unfocusedTextColor = Color.White.copy(alpha = 0.6f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF2A2A2A))
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color.White) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSlider(
    label: String,
    range: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChange: (Float) -> Unit,
    minLabel: String,
    maxLabel: String,
    valueFormatter: (Float) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = valueFormatter(value),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right
            )
        }
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            steps = 0,
            interactionSource = interactionSource,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFBF7A54),
                activeTrackColor = Color(0xFFBF7A54),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            thumb = {
                val thumbInteractionSource = remember { MutableInteractionSource() }
                Spacer(
                    modifier = Modifier
                        .size(16.dp)
                        .indication(
                            interactionSource = thumbInteractionSource,
                            indication = ripple(
                                bounded = false,
                                radius = 20.dp
                            )
                        )
                        .hoverable(interactionSource = thumbInteractionSource)
                        .background(color = Color(0xFFBF7A54), shape = CircleShape)
                        .padding(3.dp)
                        .background(color = Color(0xFFD9D9D9), shape = CircleShape)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(6.dp),
                    enabled = true,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFBF7A54),
                        activeTrackColor = Color.White.copy(alpha = 0.2f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = minLabel, color = Color.White, fontSize = 16.sp)
            Text(text = maxLabel, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun YourParametersScreenPreview() {
    YourParametersScreen()
}