package org.telegram.divo.screen.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.components.items.ParameterBottomSheet
import org.telegram.divo.components.items.ParameterItem
import org.telegram.divo.components.items.ParametersBlock
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.screen.add_model.CountryPickerSheet
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.search.LocalCity
import org.telegram.divo.screen.search.State
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBottomSheet(
    uiState: State,
    onApply: (
        countries: List<LocalCountry>,
        city: LocalCity?,
        role: ProfileParameter,
        gender: ProfileParameter,
        hairLength: ProfileParameter,
        hairColor: ProfileParameter,
        eyeColor: ProfileParameter,
        skinColor: ProfileParameter,
        blockParams: List<ProfileParameter>
    ) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var draftCountries by remember { mutableStateOf(uiState.selectedCountries) }
    var draftCity by remember { mutableStateOf(uiState.selectedCity) }
    var draftGender by remember { mutableStateOf(uiState.gender) }
    var draftRole by remember { mutableStateOf(uiState.role) }
    var draftHairLength by remember { mutableStateOf(uiState.hairLength) }
    var draftHairColor by remember { mutableStateOf(uiState.hairColor) }
    var draftEyeColor by remember { mutableStateOf(uiState.eyeColor) }
    var draftSkinColor by remember { mutableStateOf(uiState.skinColor) }

    var draftBlockParams by remember {
        mutableStateOf(uiState.blockParams.ifEmpty { uiState.getDefaultBlockParams() })
    }

    var showParametersSheet by remember { mutableStateOf(false) }
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var showCitySheet by rememberSaveable { mutableStateOf(false) }

    var showMoreParameters by remember { mutableStateOf(false) }

    var currentParam by remember { mutableStateOf<ParametersType?>(null) }
    var currentParamOptions by remember { mutableStateOf<List<String>?>(null) }
    var currentValue by remember { mutableStateOf("") }

    val openBottomSheet = { param: ParametersType, options: List<String>?, value: String ->
        currentParam = param
        currentParamOptions = options
        currentValue = value
        showParametersSheet = true
    }

    if (showCountrySheet) {
        CountryPickerSheet(
            list = uiState.allCountries,
            selectedCountries = draftCountries,
            isMultiSelection = true,
            onDismiss = { showCountrySheet = false },
            onPick = { selectedList ->
                draftCountries = selectedList

                // 1. Сбрасываем город, если новая выбранная страна не совпадает со страной города
                if (draftCountries.isNotEmpty()) {
                    draftCity?.let { currentCity ->
                        val allowedCountryCodes = draftCountries.map { it.shortName.uppercase() }
                        if (currentCity.countryCode.uppercase() !in allowedCountryCodes) {
                            draftCity = null
                        }
                    }
                }

                showCountrySheet = false
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
            onDismiss = { showParametersSheet = false },
            onSave = { selectedValue ->
                currentParam?.let { paramType ->
                    when (paramType) {
                        ParametersType.ROLE -> draftRole = uiState.role.copy(value = selectedValue)
                        ParametersType.GENDER -> draftGender = uiState.gender.copy(value = selectedValue)
                        ParametersType.HAIR_LENGTH -> draftHairLength = uiState.hairLength.copy(value = selectedValue)
                        ParametersType.HAIR_COLOR -> draftHairColor = uiState.hairColor.copy(value = selectedValue)
                        ParametersType.EYE_COLOR -> draftEyeColor = uiState.eyeColor.copy(value = selectedValue)
                        ParametersType.SKIN_COLOR -> draftSkinColor = uiState.skinColor.copy(value = selectedValue)
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
                        ParametersType.ROLE -> draftRole = draftRole.copy(value = "")
                        ParametersType.GENDER -> draftGender = uiState.gender.copy(value = "")
                        ParametersType.HAIR_LENGTH -> draftHairLength = uiState.hairLength.copy(value = "")
                        ParametersType.HAIR_COLOR -> draftHairColor = uiState.hairColor.copy(value = "")
                        ParametersType.EYE_COLOR -> draftEyeColor = uiState.eyeColor.copy(value = "")
                        ParametersType.SKIN_COLOR -> draftSkinColor = uiState.skinColor.copy(value = "")
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
            draftCountries = emptyList()
            draftCity = null
            draftRole = ProfileParameter(ParametersType.ROLE, "")
            draftGender = ProfileParameter(ParametersType.GENDER, "")
            draftHairLength = uiState.hairLength.copy(value = "")
            draftHairColor = uiState.hairColor.copy(value = "")
            draftEyeColor = uiState.eyeColor.copy(value = "")
            draftSkinColor = uiState.skinColor.copy(value = "")
            draftBlockParams = draftBlockParams.map { it.copy(value = "") }

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
                Spacer(Modifier.height(16.dp))
                ParameterItem(
                    param = draftRole.copy(value = if (draftRole.value.isEmpty()) stringResource(R.string.AllLabel) else uiState.getFormatedOptions(draftRole)),
                    onClick = { openBottomSheet(draftRole.type, context.resources.getStringArray(R.array.ModelFunNewTalent).toList(), draftRole.value) }
                )

                Spacer(Modifier.height(16.dp))
                ParameterItem(
                    param = draftGender.copy(value = if (draftGender.value.isEmpty()) stringResource(R.string.AllGendersLabel) else uiState.getFormatedOptions(draftGender)),
                    onClick = { openBottomSheet(draftGender.type, context.resources.getStringArray(R.array.GenderItems).toList(), draftGender.value) }
                )

                Spacer(Modifier.height(16.dp))
                ParameterItem(
                    param = ProfileParameter(
                        type = ParametersType.COUNTRY,
                        value = if (draftCountries.isEmpty()) stringResource(R.string.CountriesValue) else uiState.getFormatedCountries(draftCountries)
                    ),
                    weightLabel = 0.4f,
                    weightValue = 0.6f,
                    onClick = { showCountrySheet = true }
                )
                Spacer(Modifier.height(16.dp))
                CityField(
                    text = draftCity?.name.orEmpty(),
                    onClick = { showCitySheet = true }
                )

                Spacer(Modifier.height(16.dp))

                if (!uiState.isModel && !showMoreParameters) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.clickableWithoutRipple { showMoreParameters = true},
                            text = stringResource(R.string.MoreParameters),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 15.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                }

                if (showMoreParameters) {
                    ParametersBlock(
                        items = draftBlockParams,
                        onClick = { param -> openBottomSheet(param.type, null, param.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftHairLength.copy(value = if (draftHairLength.value.isEmpty()) stringResource(R.string.AllLengthsLabel) else uiState.getFormatedOptions(draftHairLength)),
                        onClick = { openBottomSheet(draftHairLength.type, listOf(context.getString(R.string.AllLengthsLabel)) + uiState.hairLengthOptions.map { it.title.orEmpty() }, draftHairLength.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftHairColor.copy(value = if (draftHairColor.value.isEmpty()) stringResource(R.string.AllColorsLabel) else uiState.getFormatedOptions(draftHairColor)),
                        onClick = { openBottomSheet(draftHairColor.type, listOf(context.getString(R.string.AllColorsLabel)) + uiState.hairColorOptions.map { it.title.orEmpty() }, draftHairColor.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftEyeColor.copy(value = if (draftEyeColor.value.isEmpty()) stringResource(R.string.AllColorsLabel) else uiState.getFormatedOptions(draftEyeColor)),
                        onClick = { openBottomSheet(draftEyeColor.type, listOf(context.getString(R.string.AllColorsLabel)) + uiState.eyeColorOptions.map { it.title.orEmpty() }, draftEyeColor.value) }
                    )
                    Spacer(Modifier.height(16.dp))
                    ParameterItem(
                        param = draftSkinColor.copy(value = if (draftSkinColor.value.isEmpty()) stringResource(R.string.AllColorsLabel) else uiState.getFormatedOptions(draftSkinColor)),
                        onClick = { openBottomSheet(draftSkinColor.type, listOf(context.getString(R.string.AllColorsLabel)) + uiState.skinColorOptions.map { it.title.orEmpty() }, draftSkinColor.value) }
                    )
                }

                if (showMoreParameters) {
                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.clickableWithoutRipple { showMoreParameters = false },
                            text = stringResource(R.string.LessParameters),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 15.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                text = stringResource(R.string.ApplyFilter),
                onClick = {
                    onApply(draftCountries, draftCity, draftRole, draftGender, draftHairLength, draftHairColor, draftEyeColor, draftSkinColor, draftBlockParams)
                    onDismiss()
                }
            )
        }
    }
}
