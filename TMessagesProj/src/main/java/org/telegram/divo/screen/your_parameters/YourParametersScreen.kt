package org.telegram.divo.screen.your_parameters

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.toDateFloat
import org.telegram.divo.components.DivoSlider
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun YourParametersScreen(
    viewModel: YourParametersViewModel = viewModel(),
    showTitle: Boolean = true,
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val parametersSavedText = stringResource(R.string.ParametersSaved)
    val touched = state.touchedFields

    LaunchedEffect(Unit) {
        viewModel.setIntent(YourParametersIntent.OnLoad)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is YourParametersEffect.SaveSuccess -> {
                    Toast.makeText(context, parametersSavedText, Toast.LENGTH_SHORT).show()
                    onSaved()
                }
                is YourParametersEffect.NavigateBack -> {
                    onBack()
                }
                is YourParametersEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222222))
        ) {
            val headerHeight = 44.dp
            val bottomBarHeight = 74.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showTitle) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.YourParameters).uppercase(),
                            style = AppTheme.typography.helveticaNeueLtCom,
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }
                }
                ParameterSelector(
                    label = stringResource(R.string.SelectGender),
                    items = stringArrayResource(R.array.GenderItems).toList(),
                    selectedValue = state.userFull.gender.title.takeIf { ParameterField.GENDER in touched } ?: "",
                    onItemSelected = { _, title ->
                        viewModel.setIntent(YourParametersIntent.OnGenderChanged(title))
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                ParameterSlider(
                    label = stringResource(R.string.LabelAge),
                    range = 14f..45f,
                    value = state.userFull.birthday.toDateFloat(),
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersIntent.OnAgeChanged(
                                it
                            )
                        )
                    },
                    minLabel = stringResource(R.string.AgeMin),
                    maxLabel = stringResource(R.string.AgeMax),
                    valueFormatter = { v -> "${v.toInt()} y.o" }
                )
                ParameterSlider(
                    label = stringResource(R.string.LabelHeight),
                    range = 140f..220f,
                    value = state.userFull.model.appearance.height,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersIntent.OnHeightChanged(
                                it
                            )
                        )
                    },
                    minLabel = stringResource(R.string.HeightMin),
                    maxLabel = stringResource(R.string.HeightMax),
                    valueFormatter = { v -> "${v.toInt()} cm" }
                )
                ParameterSlider(
                    label = stringResource(R.string.LabelWaist),
                    range = 48f..90f,
                    minLabel = stringResource(R.string.WaistMin),
                    maxLabel = stringResource(R.string.WaistMax),
                    value = state.userFull.model.appearance.waist,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersIntent.OnWaistChanged(
                                it
                            )
                        )
                    },
                    valueFormatter = { v -> "${v.toInt()} cm" }
                )
                ParameterSlider(
                    label = stringResource(R.string.LabelHips),
                    range = 80f..110f,
                    minLabel = stringResource(R.string.HipsMin),
                    maxLabel = stringResource(R.string.HipsMax),
                    value = state.userFull.model.appearance.hips,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersIntent.OnHipsChanged(
                                it
                            )
                        )
                    },
                    valueFormatter = { v -> "${v.toInt()} cm" }
                )
                ParameterSlider(
                    label = stringResource(R.string.LabelShoeSize),
                    range = 36f..42f,
                    minLabel = stringResource(R.string.ShoeSizeMin),
                    maxLabel = stringResource(R.string.ShoeSizeMax),
                    value = state.userFull.model.appearance.shoesSize,
                    onValueChange = {
                        viewModel.setIntent(
                            YourParametersIntent.OnShoeSizeChanged(
                                it
                            )
                        )
                    },
                    valueFormatter = { v -> v.toInt().toString() }
                )
                Spacer(modifier = Modifier.height(20.dp))
                ParameterSelector(
                    label = stringResource(R.string.ChooseHairLength),
                    items = stringArrayResource(R.array.HairLengthItems).toList(),
                    selectedValue = state.userFull.model.appearance.hairLength.title.takeIf { ParameterField.HAIR_LENGTH in touched } ?: "",
                    onItemSelected = { index, title ->
                        viewModel.setIntent(YourParametersIntent.OnHairLengthChanged(index, title))
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ParameterSelector(
                    label = stringResource(R.string.ChooseHairColor),
                    items = stringArrayResource(R.array.HairColorItems).toList(),
                    selectedValue = state.userFull.model.appearance.hairColor.title.takeIf { ParameterField.HAIR_COLOR in touched } ?: "",
                    onItemSelected = { index, title ->
                        viewModel.setIntent(YourParametersIntent.OnHairColorChanged(index, title))
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ParameterSelector(
                    label = stringResource(R.string.ChooseEyeColor),
                    items = stringArrayResource(R.array.EyeColorItems).toList(),
                    selectedValue = state.userFull.model.appearance.eyeColor.title.takeIf { ParameterField.EYE_COLOR in touched } ?: "",
                    onItemSelected = { index, title ->
                        viewModel.setIntent(YourParametersIntent.OnEyeColorChanged(index, title))
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ParameterSelector(
                    label = stringResource(R.string.ChooseSkinColor),
                    items = stringArrayResource(R.array.SkinColorItems).toList(),
                    selectedValue = state.userFull.model.appearance.skinColor.title.takeIf { ParameterField.SKIN_COLOR in touched } ?: "",
                    onItemSelected = { index, title ->
                        viewModel.setIntent(YourParametersIntent.OnSkinColorChanged(index, title))
                    }
                )
                if (state.userFull.gender.title == stringResource(R.string.Female)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val items = stringArrayResource(R.array.BreastSizeItems).toList()
                    ParameterSelector(
                        label = stringResource(R.string.ChooseBreastSize),
                        items = items,
                        selectedValue = state.userFull.model.appearance.breastSize.takeIf { ParameterField.BREAST_SIZE in touched } ?: "",
                        onItemSelected = { index, _ ->
                            viewModel.setIntent(YourParametersIntent.OnBreastSizeChanged(items[index - 1]))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(bottomBarHeight + 16.dp))
            }

            // Fixed bottom buttons (not scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.2f to Color(0xFF222222),
                                1.0f to Color(0xFF222222)
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.setIntent(YourParametersIntent.OnBackClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3A)),
                    enabled = !state.isLoading
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        Spacer(Modifier.width(2.dp))
                        Text(text = stringResource(R.string.ButtonBack), color = Color.White, fontSize = 18.sp)
                    }
                }
                Button(
                    onClick = { viewModel.setIntent(YourParametersIntent.OnSaveClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF7A54)),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        LottieProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(text = stringResource(R.string.Saved), color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

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

        DivoSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
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