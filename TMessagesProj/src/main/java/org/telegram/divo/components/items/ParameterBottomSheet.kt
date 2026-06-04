package org.telegram.divo.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoRangeSlider
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.messenger.R.string
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    paramType: ParametersType?,
    options: List<String>? = null,
    isMultiSelect: Boolean = false,
    initialValue: String = "",
    iconClose: Int = R.drawable.ic_divo_back,
    useNumericRangeUi: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isDatePicker = paramType == ParametersType.BIRTHDAY
    val isAgePicker = paramType == ParametersType.AGE
    val numericFilterBounds = remember(paramType, useNumericRangeUi, options) {
        if (useNumericRangeUi && options.isNullOrEmpty()) paramType?.numericFilterRange() else null
    }
    val isNumericRangePicker = numericFilterBounds != null && !isDatePicker && !isAgePicker

    val ageBounds = ParametersType.AGE.numericFilterRange() ?: (16..45)
    val ageParts = if (isAgePicker) initialValue.split("-") else emptyList()
    var selectedMinAge by remember { mutableIntStateOf(ageParts.getOrNull(0)?.toIntOrNull() ?: ageBounds.first) }
    var selectedMaxAge by remember { mutableIntStateOf(ageParts.getOrNull(1)?.toIntOrNull() ?: ageBounds.last) }

    val dateParts = if (isDatePicker) initialValue.split("-") else emptyList()
    var selectedYear by remember { mutableStateOf(dateParts.getOrNull(0)?.takeIf { it.length == 4 } ?: "2000") }
    var selectedMonth by remember { mutableStateOf(dateParts.getOrNull(1) ?: "01") }
    var selectedDay by remember { mutableStateOf(dateParts.getOrNull(2) ?: "01") }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        val year = selectedYear.toIntOrNull() ?: 2000
        val month = selectedMonth.toIntOrNull() ?: 1
        when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    LaunchedEffect(daysInMonth) {
        val currentDay = selectedDay.toIntOrNull() ?: 1
        if (currentDay > daysInMonth) {
            selectedDay = daysInMonth.toString().padStart(2, '0')
        }
    }

    val integerParts = remember(paramType) {
        val range = when (paramType) {
            ParametersType.HEIGHT -> 120..220
            ParametersType.WEIGHT -> 30..200
            ParametersType.WAIST -> 40..130
            ParametersType.HIPS -> 60..150
            ParametersType.SHOE_SIZE -> 32..50
            else -> 0..250
        }
        range.map { it.toString() }
    }
    var selectedIntPart by remember(initialValue, integerParts) {
        val parsed = initialValue.substringBefore(".").takeIf { it.isNotEmpty() }
        mutableStateOf(parsed.takeIf { integerParts.contains(it) } ?: integerParts[integerParts.size / 2])
    }

    var selectedOptions by remember(initialValue, options) {
        mutableStateOf(
            if (initialValue.isNotBlank()) {
                initialValue.split(", ").toSet()
            } else if (!options.isNullOrEmpty()) {
                setOf(options.first())
            } else {
                emptySet()
            }
        )
    }

    val numericRangeInitial = remember(initialValue, numericFilterBounds) {
        if (numericFilterBounds != null) resolveNumericBlockParamBounds(initialValue, numericFilterBounds)
        else 0 to 0
    }
    var selectedMinNumeric by remember(initialValue, numericFilterBounds, paramType) {
        mutableIntStateOf(numericRangeInitial.first)
    }
    var selectedMaxNumeric by remember(initialValue, numericFilterBounds, paramType) {
        mutableIntStateOf(numericRangeInitial.second)
    }

    DivoBottomSheet(
        sheetState = sheetState,
        title = paramType?.titleRes?.let { stringResource(it) }.orEmpty(),
        iconClose = iconClose,
        onDismiss = onDismiss,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onSave = {
            if (isAgePicker) {
                onSave("$selectedMinAge-$selectedMaxAge")
            } else if (isNumericRangePicker) {
                onSave("$selectedMinNumeric-$selectedMaxNumeric")
            } else if (isDatePicker) {
                onSave("$selectedYear-$selectedMonth-$selectedDay")
            } else if (options.isNullOrEmpty()) {
                onSave(selectedIntPart)
            } else {
                val defaultOption = options.firstOrNull()
                if (isMultiSelect && selectedOptions == setOf(defaultOption)) {
                    onSave("")
                } else {
                    onSave(selectedOptions.joinToString(", "))
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        val itemHeight = 40.dp
        Column(modifier = Modifier.fillMaxSize()) {
            if (isAgePicker) {
                AgeRangeSelector(
                    minAge = selectedMinAge,
                    maxAge = selectedMaxAge,
                    onAgeChange = { newMin, newMax ->
                        selectedMinAge = newMin
                        selectedMaxAge = newMax
                    }
                )
            } else if (isNumericRangePicker && numericFilterBounds != null) {
                NumericRangeSelector(
                    bounds = numericFilterBounds,
                    minVal = selectedMinNumeric,
                    maxVal = selectedMaxNumeric,
                    onValueChange = { newMin, newMax ->
                        selectedMinNumeric = newMin
                        selectedMaxNumeric = newMax
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.onBackground)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(41.dp))
                            .background(Color(0xFFEBEBEB), RoundedCornerShape(8.dp))
                    )

                    if (isDatePicker) {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val maxYear = currentYear - 14

                        val daysList = (1..daysInMonth).map { it.toString().padStart(2, '0') }
                        val monthsList = (1..12).map { it.toString().padStart(2, '0') }
                        val yearsList = (1950..maxYear).map { it.toString() }.reversed()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            DivoWheelPicker(
                                items = daysList,
                                initialIndex = daysList.indexOf(selectedDay).coerceAtLeast(0),
                                isCyclic = false,
                                modifier = Modifier.weight(1f),
                                onItemSelected = { _, item -> selectedDay = item }
                            )
                            DivoWheelPicker(
                                items = monthsList,
                                initialIndex = monthsList.indexOf(selectedMonth).coerceAtLeast(0),
                                isCyclic = false,
                                modifier = Modifier.weight(1f),
                                onItemSelected = { _, item -> selectedMonth = item }
                            )
                            DivoWheelPicker(
                                items = yearsList,
                                initialIndex = yearsList.indexOf(selectedYear).coerceAtLeast(0),
                                isCyclic = false,
                                modifier = Modifier.weight(1f),
                                onItemSelected = { _, item -> selectedYear = item }
                            )
                        }
                    } else if (!options.isNullOrEmpty()) {
                        OptionsBlock(
                            items = options,
                            selectedItems = selectedOptions,
                            onClick = { clickedItem ->
                                if (isMultiSelect) {
                                    val defaultOption = options.first() // Наш пункт "Все" (первый в списке)

                                    if (clickedItem == defaultOption) {
                                        // 1. Если выбрали "Все" -> убираем остальные галочки
                                        selectedOptions = setOf(defaultOption)
                                    } else {
                                        // 2. Если кликают по другим пунктам
                                        val newSelection = if (selectedOptions.contains(clickedItem)) {
                                            selectedOptions - clickedItem // Снимаем галочку
                                        } else {
                                            // Ставим галочку + заодно убираем галочку с "Все"
                                            (selectedOptions - defaultOption) + clickedItem
                                        }

                                        // 3. Если после клика не осталось ни одной галочки -> возвращаем на "Все"
                                        selectedOptions = newSelection.ifEmpty { setOf(defaultOption) }
                                    }
                                } else {
                                    // Логика одиночного выбора остается простой
                                    selectedOptions = setOf(clickedItem)
                                }
                            }
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            DivoWheelPicker(
                                items = integerParts,
                                initialIndex = integerParts.indexOf(selectedIntPart)
                                    .coerceAtLeast(0),
                                isCyclic = true,
                                modifier = Modifier.width(70.dp),
                                onItemSelected = { _, item -> selectedIntPart = item }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            UIButtonNew(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(string.ResetParameter),
                height = 48.dp,
                background = AppTheme.colors.buttonSecondary,
                onClick = onDelete
            )
        }
    }
}

@Composable
fun AgeRangeSelector(
    minAge: Int,
    maxAge: Int,
    onAgeChange: (Int, Int) -> Unit
) {
    val bounds = ParametersType.AGE.numericFilterRange() ?: (16..45)
    NumericRangeSelector(
        bounds = bounds,
        minVal = minAge,
        maxVal = maxAge,
        maxDigits = 2,
        onValueChange = onAgeChange
    )
}

@Composable
fun NumericRangeSelector(
    bounds: IntRange,
    minVal: Int,
    maxVal: Int,
    maxDigits: Int = 3,
    onValueChange: (Int, Int) -> Unit,
) {
    val floatRange = bounds.first.toFloat()..bounds.last.toFloat()

    var minText by remember(minVal) { mutableStateOf(minVal.toString()) }
    var maxText by remember(maxVal) { mutableStateOf(maxVal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AgeTextField(
                modifier = Modifier.weight(1f),
                value = minText,
                onValueChange = { newText ->
                    if (newText.length <= maxDigits && newText.all { it.isDigit() }) {
                        minText = newText
                        val parsed = newText.toIntOrNull()
                        if (parsed != null && parsed in bounds.first..maxVal) {
                            onValueChange(parsed, maxVal)
                        }
                    }
                },
                onFocusLost = {
                    val parsed = minText.toIntOrNull() ?: minVal
                    val clamped = parsed.coerceIn(bounds.first, maxVal)
                    minText = clamped.toString()
                    onValueChange(clamped, maxVal)
                }
            )

            Text(
                text = "—",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Black,
                fontSize = 18.sp
            )

            AgeTextField(
                modifier = Modifier.weight(1f),
                value = maxText,
                onValueChange = { newText ->
                    if (newText.length <= maxDigits && newText.all { it.isDigit() }) {
                        maxText = newText
                        val parsed = newText.toIntOrNull()
                        if (parsed != null && parsed in minVal..bounds.last) {
                            onValueChange(minVal, parsed)
                        }
                    }
                },
                onFocusLost = {
                    val parsed = maxText.toIntOrNull() ?: maxVal
                    val clamped = parsed.coerceIn(minVal, bounds.last)
                    maxText = clamped.toString()
                    onValueChange(minVal, clamped)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DivoRangeSlider(
            range = floatRange,
            currentMin = minVal.toFloat(),
            currentMax = maxVal.toFloat(),
            onValueChange = { newMin, newMax ->
                onValueChange(newMin.roundToInt(), newMax.roundToInt())
            }
        )
    }
}

@Composable
private fun OptionsBlock(
    items: List<String>,
    selectedItems: Set<String>,
    onClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItems.contains(item)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clickableWithoutRipple { onClick(item) },
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp),
                    text = item,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isSelected) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_divo_apply),
                        tint = AppTheme.colors.accentOrange,
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (index != items.size - 1) {
                Divider()
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(AppTheme.colors.accentOrange),
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
            .onFocusChanged { if (!it.isFocused) onFocusLost() },
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField()
            }
        }
    )
}

