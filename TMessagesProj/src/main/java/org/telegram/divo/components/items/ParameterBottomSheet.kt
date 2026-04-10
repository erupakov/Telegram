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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    initialValue: String = "",
    iconClose: Int = R.drawable.ic_divo_back,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isDatePicker = paramType == ParametersType.BIRTHDAY
    val isAgePicker = paramType == ParametersType.AGE

    val ageParts = if (isAgePicker) initialValue.split("-") else emptyList()
    var selectedMinAge by remember { mutableIntStateOf(ageParts.getOrNull(0)?.toIntOrNull() ?: 14) }
    var selectedMaxAge by remember { mutableIntStateOf(ageParts.getOrNull(1)?.toIntOrNull() ?: 45) }

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
            ParametersType.WAIST -> 40..130
            ParametersType.HIPS -> 60..150
            ParametersType.SHOE_SIZE -> 30..50
            else -> 0..250
        }
        range.map { it.toString() }
    }
    val decimalParts = remember { (0..9).map { it.toString() } }

    val initialOptionIndex = remember(options, initialValue) {
        options?.indexOf(initialValue)?.takeIf { it >= 0 } ?: 0
    }
    var selectedSingleOption by remember {
        mutableStateOf(options?.getOrNull(initialOptionIndex) ?: "")
    }

    var selectedIntPart by remember(initialValue, integerParts) {
        val parsed = initialValue.substringBefore(".").takeIf { it.isNotEmpty() }
        mutableStateOf(parsed.takeIf { integerParts.contains(it) } ?: integerParts[integerParts.size / 2])
    }
    var selectedDecPart by remember(initialValue) {
        mutableStateOf(if (initialValue.contains(".")) initialValue.substringAfter(".") else "0")
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
            } else if (isDatePicker) {
                onSave("$selectedYear-$selectedMonth-$selectedDay")
            } else if (options.isNullOrEmpty()) {
                onSave("$selectedIntPart.$selectedDecPart")
            } else {
                onSave(selectedSingleOption)
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
                        DivoWheelPicker(
                            items = options,
                            initialIndex = initialOptionIndex,
                            isCyclic = true,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            onItemSelected = { _, item -> selectedSingleOption = item }
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

                            DivoWheelPicker(
                                items = decimalParts,
                                initialIndex = decimalParts.indexOf(selectedDecPart)
                                    .coerceAtLeast(0),
                                isCyclic = true,
                                modifier = Modifier.width(70.dp),
                                onItemSelected = { _, item -> selectedDecPart = item }
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
    val ageRange = 14f..45f

    var minText by remember(minAge) { mutableStateOf(minAge.toString()) }
    var maxText by remember(maxAge) { mutableStateOf(maxAge.toString()) }

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
                    if (newText.length <= 2 && newText.all { it.isDigit() }) {
                        minText = newText
                        val parsed = newText.toIntOrNull()
                        if (parsed != null && parsed in 14..maxAge) {
                            onAgeChange(parsed, maxAge)
                        }
                    }
                },
                onFocusLost = {
                    val parsed = minText.toIntOrNull() ?: minAge
                    val clamped = parsed.coerceIn(14, maxAge)
                    minText = clamped.toString()
                    onAgeChange(clamped, maxAge)
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
                    if (newText.length <= 2 && newText.all { it.isDigit() }) {
                        maxText = newText
                        val parsed = newText.toIntOrNull()
                        if (parsed != null && parsed in minAge..45) {
                            onAgeChange(minAge, parsed)
                        }
                    }
                },
                onFocusLost = {
                    val parsed = maxText.toIntOrNull() ?: maxAge
                    val clamped = parsed.coerceIn(minAge, 45)
                    maxText = clamped.toString()
                    onAgeChange(minAge, clamped)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DivoRangeSlider(
            range = ageRange,
            currentMin = minAge.toFloat(),
            currentMax = maxAge.toFloat(),
            onValueChange = { newMin, newMax ->
                onAgeChange(newMin.roundToInt(), newMax.roundToInt())
            }
        )
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

