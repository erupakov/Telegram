package org.telegram.divo.screen.event_list.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.components.bottomsheets.DivoBottomSheet
import org.telegram.divo.components.bottomsheets.ParameterBottomSheet
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ParameterItem
import org.telegram.divo.components.items.ParametersBlock
import org.telegram.divo.entity.EventType
import org.telegram.divo.screen.event_list.EventSearchFilters
import org.telegram.divo.screen.event_list.EventListViewState
import org.telegram.divo.screen.event_list.getDefaultEventBlockParams
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.divo.common.utils.getFormatedOptions
import org.telegram.divo.screen.reg_form.components.PlaceField
import org.telegram.divo.components.bottomsheets.CityPickerSheet
import androidx.compose.ui.platform.LocalContext
import org.telegram.divo.entity.LocalCountry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFilterBottomSheet(
    currentFilters: EventSearchFilters,
    uiState: EventListViewState,
    onApply: (EventSearchFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var draftTypes by remember { mutableStateOf(currentFilters.selectedEventTypes) }
    var draftCity by remember { mutableStateOf(currentFilters.city) }
    var draftPaidOnly by remember { mutableStateOf(currentFilters.paidOnly) }
    var draftAgeFrom by remember { mutableStateOf(currentFilters.ageFrom) }
    var draftAgeTo by remember { mutableStateOf(currentFilters.ageTo) }
    
    var draftBlockParams by remember { mutableStateOf(currentFilters.blockParams) }
    var draftHairLength by remember { mutableStateOf(currentFilters.hairLength) }
    var draftHairColor by remember { mutableStateOf(currentFilters.hairColor) }
    var draftEyeColor by remember { mutableStateOf(currentFilters.eyeColor) }
    var draftSkinColor by remember { mutableStateOf(currentFilters.skinColor) }

    var showTypePickerSheet by remember { mutableStateOf(false) }
    var showAgeSheet by remember { mutableStateOf(false) }
    var showMoreParameters by remember { mutableStateOf(false) }
    
    var showCitySheet by remember { mutableStateOf(false) }
    var draftCountries by remember { mutableStateOf<List<LocalCountry>>(
        uiState.allCountries.filter { c ->
            currentFilters.city?.countryCode.equals(c.shortName, ignoreCase = true)
        }
    ) }
    
    var showParametersSheet by remember { mutableStateOf(false) }
    var currentParam by remember { mutableStateOf<ParametersType?>(null) }
    var currentParamOptions by remember { mutableStateOf<List<String>?>(null) }
    var currentValue by remember { mutableStateOf("") }

    val openBottomSheet = { param: ParametersType, options: List<String>?, value: String ->
        currentParam = param
        currentParamOptions = options
        currentValue = value
        showParametersSheet = true
    }

    if (showTypePickerSheet) {
        EventTypePickerSheet(
            eventTypes = uiState.eventTypes,
            selectedTypes = draftTypes,
            onDismiss = { showTypePickerSheet = false },
            onPick = { selected ->
                draftTypes = selected
                showTypePickerSheet = false
            }
        )
    }

    if (showAgeSheet) {
        ParameterBottomSheet(
            paramType = ParametersType.AGE,
            options = null,
            isMultiSelect = false,
            initialValue = if (draftAgeFrom != null && draftAgeTo != null) "${draftAgeFrom}-${draftAgeTo}" else "",
            iconClose = R.drawable.ic_divo_back,
            useNumericRangeUi = true,
            onDismiss = { showAgeSheet = false },
            onSave = { selectedValue ->
                if ("-" in selectedValue) {
                    val parts = selectedValue.split("-").mapNotNull { it.trim().toIntOrNull() }
                    if (parts.size >= 2) {
                        draftAgeFrom = parts[0]
                        draftAgeTo = parts[1]
                    }
                } else {
                    val intValue = selectedValue.toIntOrNull()
                    draftAgeFrom = intValue
                    draftAgeTo = intValue
                }
                showAgeSheet = false
            },
            onDelete = {
                draftAgeFrom = null
                draftAgeTo = null
                showAgeSheet = false
            }
        )
    }

    if (showCitySheet) {
        CityPickerSheet(
            list = uiState.allCities,
            selectedCity = draftCity,
            allCountries = uiState.allCountries,
            selectedCountries = draftCountries,
            onDismiss = { showCitySheet = false },
            onPick = { selectedCity ->
                draftCity = selectedCity

                if (selectedCity != null) {
                    val cityCountry = uiState.allCountries.find {
                        it.shortName.equals(selectedCity.countryCode, ignoreCase = true)
                    }
                    if (cityCountry != null) {
                        draftCountries = listOf(cityCountry)
                    }
                }

                showCitySheet = false
            }
        )
    }

    if (showParametersSheet) {
        ParameterBottomSheet(
            paramType = currentParam,
            options = currentParamOptions,
            isMultiSelect = true,
            initialValue = currentValue,
            iconClose = R.drawable.ic_divo_back,
            useNumericRangeUi = true,
            onDismiss = { showParametersSheet = false },
            onSave = { selectedValue ->
                currentParam?.let { paramType ->
                    when (paramType) {
                        ParametersType.HAIR_LENGTH -> draftHairLength = draftHairLength.copy(value = selectedValue)
                        ParametersType.HAIR_COLOR -> draftHairColor = draftHairColor.copy(value = selectedValue)
                        ParametersType.EYE_COLOR -> draftEyeColor = draftEyeColor.copy(value = selectedValue)
                        ParametersType.SKIN_COLOR -> draftSkinColor = draftSkinColor.copy(value = selectedValue)
                        else -> {
                            draftBlockParams = draftBlockParams.map { item ->
                                if (item.type == paramType) item.copy(value = selectedValue) else item
                            }
                        }
                    }
                }
                showParametersSheet = false
            },
            onDelete = {
                currentParam?.let { paramType ->
                    when (paramType) {
                        ParametersType.HAIR_LENGTH -> draftHairLength = draftHairLength.copy(value = "")
                        ParametersType.HAIR_COLOR -> draftHairColor = draftHairColor.copy(value = "")
                        ParametersType.EYE_COLOR -> draftEyeColor = draftEyeColor.copy(value = "")
                        ParametersType.SKIN_COLOR -> draftSkinColor = draftSkinColor.copy(value = "")
                        else -> {
                            draftBlockParams = draftBlockParams.map { item ->
                                if (item.type == paramType) item.copy(value = "") else item
                            }
                        }
                    }
                }
                showParametersSheet = false
            }
        )
    }

    val scrollState = rememberScrollState()

    DivoBottomSheet(
        title = stringResource(R.string.FilterResults),
        onDismiss = onDismiss,
        isSaveMode = false,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onReset = {
            draftTypes = emptyList()
            draftCity = null
            draftPaidOnly = false
            draftAgeFrom = null
            draftAgeTo = null
            draftBlockParams = getDefaultEventBlockParams()
            draftHairLength = draftHairLength.copy(value = "")
            draftHairColor = draftHairColor.copy(value = "")
            draftEyeColor = draftEyeColor.copy(value = "")
            draftSkinColor = draftSkinColor.copy(value = "")
            onReset()
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .verticalScroll(scrollState)
                    .padding(bottom = 70.dp)
            ) {
                // Type events
                Spacer(Modifier.height(16.dp))
                FilterRow(
                    label = stringResource(R.string.EventFilterTypeEvents),
                    value = if (draftTypes.isEmpty()) stringResource(R.string.EventFilterChooseEvent) else draftTypes.joinToString(", ") { it.title.orEmpty() },
                    onClick = { showTypePickerSheet = true }
                )

                // City
                Spacer(Modifier.height(16.dp))
                PlaceField(
                    text = draftCity?.let { it.matchedName ?: it.name } ?: stringResource(R.string.CityLabel),
                    label = stringResource(R.string.CityLabel),
                    onClick = { showCitySheet = true }
                )

                // Age
                Spacer(Modifier.height(16.dp))
                FilterRow(
                    label = stringResource(R.string.EventFilterAge),
                    value = if (draftAgeFrom != null && draftAgeTo != null) {
                        if (draftAgeFrom == draftAgeTo) "$draftAgeFrom"
                        else "$draftAgeFrom-$draftAgeTo"
                    } else stringResource(R.string.EventFilterSelectAgeRange),
                    onClick = { showAgeSheet = true }
                )

                // Paid only
                Spacer(Modifier.height(16.dp))
                PaidOnlyRow(
                    checked = draftPaidOnly,
                    onCheckedChange = { draftPaidOnly = it }
                )

                if (!showMoreParameters) {
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(
                            modifier = Modifier.clickableWithoutRipple { showMoreParameters = true },
                            text = stringResource(R.string.MoreParameters),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 15.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                }

                if (showMoreParameters) {
                    Spacer(Modifier.height(16.dp))
                    ParametersBlock(
                        items = draftBlockParams,
                        onClick = { param -> openBottomSheet(param.type, null, param.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftHairLength.copy(value = if (draftHairLength.value.isEmpty()) stringResource(R.string.AllLengthsLabel) else draftHairLength.getFormatedOptions()),
                        onClick = { openBottomSheet(draftHairLength.type, listOf(context.getString(R.string.AllLengthsLabel)) + uiState.hairLengthOptions.map { it.title.orEmpty() }, draftHairLength.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftHairColor.copy(value = if (draftHairColor.value.isEmpty()) stringResource(R.string.AllColorsLabel) else draftHairColor.getFormatedOptions()),
                        onClick = { openBottomSheet(draftHairColor.type, listOf(context.getString(R.string.AllColorsLabel)) + uiState.hairColorOptions.map { it.title.orEmpty() }, draftHairColor.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftEyeColor.copy(value = if (draftEyeColor.value.isEmpty()) stringResource(R.string.AllColorsLabel) else draftEyeColor.getFormatedOptions()),
                        onClick = { openBottomSheet(draftEyeColor.type, listOf(context.getString(R.string.AllColorsLabel)) + uiState.eyeColorOptions.map { it.title.orEmpty() }, draftEyeColor.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftSkinColor.copy(value = if (draftSkinColor.value.isEmpty()) stringResource(R.string.AllColorsLabel) else draftSkinColor.getFormatedOptions()),
                        onClick = { openBottomSheet(draftSkinColor.type, listOf(context.getString(R.string.AllColorsLabel)) + uiState.skinColorOptions.map { it.title.orEmpty() }, draftSkinColor.value) }
                    )

                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(
                            modifier = Modifier.clickableWithoutRipple { showMoreParameters = false },
                            text = stringResource(R.string.LessParameters),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 15.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }

            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                text = stringResource(R.string.ApplyFilter),
                onClick = {
                    onApply(
                        EventSearchFilters(
                            query = currentFilters.query,
                            selectedEventTypes = draftTypes,
                            city = draftCity,
                            paidOnly = draftPaidOnly,
                            ageFrom = draftAgeFrom,
                            ageTo = draftAgeTo,
                            blockParams = draftBlockParams,
                            hairLength = draftHairLength,
                            hairColor = draftHairColor,
                            eyeColor = draftEyeColor,
                            skinColor = draftSkinColor,
                        )
                    )
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FilterRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(41.dp))
            .background(AppTheme.colors.onBackground)
            .clickableWithoutRipple { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.6f),
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        Spacer(Modifier.padding(start = 4.dp))
        Icon(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_divo_arrow_right_20),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = AppTheme.colors.textPrimary.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun FilterInputRow(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(41.dp))
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(0.3f)
        )
        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.weight(0.7f),
            textStyle = AppTheme.typography.bodyLarge.copy(
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            ),
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = AppTheme.colors.accentOrange,
            ),
        )
    }
}

@Composable
private fun PaidOnlyRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(41.dp))
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.EventFilterPaidOnly),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppTheme.colors.backgroundLight,
                checkedTrackColor = AppTheme.colors.accentOrange,
                uncheckedThumbColor = AppTheme.colors.backgroundLight,
                uncheckedTrackColor = AppTheme.colors.textPrimary.copy(alpha = 0.2f),
                uncheckedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTypePickerSheet(
    eventTypes: List<EventType>,
    selectedTypes: List<EventType>,
    onDismiss: () -> Unit,
    onPick: (List<EventType>) -> Unit,
) {
    val selectedIds = remember { mutableStateOf(selectedTypes.map { it.id }.toSet()) }

    DivoBottomSheet(
        title = stringResource(R.string.EventFilterTypeEvents),
        onDismiss = onDismiss,
        isSaveMode = false,
        contentPadding = PaddingValues(horizontal = 16.dp),
        iconClose = R.drawable.ic_divo_back,
        onReset = {
            val allTypes = eventTypes
            onPick(allTypes)
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 70.dp),
            ) {
                // "All roles" option
                item {
                    val isAllSelected = selectedIds.value.isEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickableWithoutRipple {
                                selectedIds.value = emptySet()
                            }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.EventFilterAllRoles),
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAllSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = AppTheme.colors.accentOrange
                            )
                        }
                    }
                }
                items(eventTypes) { type ->
                    val isSelected = type.id in selectedIds.value
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickableWithoutRipple {
                                selectedIds.value = if (isSelected) {
                                    selectedIds.value - type.id
                                } else {
                                    selectedIds.value + type.id
                                }
                            }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.title.orEmpty(),
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = AppTheme.colors.accentOrange
                            )
                        }
                    }
                }
            }

            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                text = stringResource(R.string.ButtonApply),
                onClick = {
                    val selected = eventTypes.filter { it.id in selectedIds.value }
                    onPick(selected)
                }
            )
        }
    }
}
