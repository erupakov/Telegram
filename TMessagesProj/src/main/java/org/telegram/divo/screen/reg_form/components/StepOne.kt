package org.telegram.divo.screen.reg_form.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.RegTextField
import org.telegram.divo.components.items.ParameterBottomSheet
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.screen.add_model.CountryPickerSheet
import org.telegram.divo.screen.reg_form.RegFormsIntent
import org.telegram.divo.screen.reg_form.RegFormsState
import org.telegram.divo.screen.reg_form.RegistrationFormData
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.screen.search.components.CityPickerSheet
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepOne(
    formData: RegistrationFormData,
    state: RegFormsState,
    onIntent: (RegFormsIntent) -> Unit,
) {
    val config = formData.subRole.identityStepConfig()
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var showCitySheet by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showGenderSheet by rememberSaveable { mutableStateOf(false) }

    if (showCountrySheet) {
        CountryPickerSheet(
            list = state.allCountries,
            selectedCountries = listOfNotNull(
                state.allCountries.find { it.name == formData.country }
            ),
            onDismiss = { showCountrySheet = false },
            onPick = { selectedList ->
                selectedList.firstOrNull()?.let { country ->
                    onIntent(RegFormsIntent.OnFieldChanged {
                        copy(country = country.name, city = "") // сбрасываем город при смене страны
                    })
                }
                showCountrySheet = false
            }
        )
    }

    if (showCitySheet) {
        CityPickerSheet(
            list = state.allCities,
            selectedCity = state.allCities.find { it.name == formData.city },
            allCountries = state.allCountries,
            selectedCountries = listOfNotNull(
                state.allCountries.find { it.name == formData.country }
            ),
            onDismiss = { showCitySheet = false },
            onPick = { selectedCity ->
                onIntent(RegFormsIntent.OnFieldChanged {
                    copy(city = selectedCity?.name.orEmpty())
                })
                showCitySheet = false
            }
        )
    }

    if (showDateSheet) {
        ParameterBottomSheet(
            paramType = ParametersType.BIRTHDAY,
            initialValue = formData.dateOfBirth.orEmpty(),
            onDismiss = { showDateSheet = false },
            onSave = { value ->
                onIntent(RegFormsIntent.OnFieldChanged { copy(dateOfBirth = value) })
                showDateSheet = false
            },
            onDelete = { showDateSheet = false } // в регистрации не нужно, просто закрываем
        )
    }

    if (showGenderSheet) {
        ParameterBottomSheet(
            paramType = ParametersType.GENDER,
            options = stringArrayResource(R.array.GenderItems).toList(),
            initialValue = formData.gender.orEmpty(),
            onDismiss = { showGenderSheet = false },
            onSave = { value ->
                onIntent(RegFormsIntent.OnFieldChanged { copy(gender = value) })
                showGenderSheet = false
            },
            onDelete = { showGenderSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(config.titleRes).uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(24.dp))

        if (config.showCompanyName) {
            RegTextField(
                value = formData.companyName,
                placeholder = if (formData.subRole == SubRole.STUDIO) stringResource(R.string.RegFormStudioName) else stringResource(R.string.RegFormCompanyName),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(companyName = it) })
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showFirstName) {
            RegTextField(
                value = formData.firstName,
                placeholder = stringResource(R.string.RegFormFirstName),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(firstName = it) })
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showLastName) {
            RegTextField(
                value = formData.lastName,
                placeholder = stringResource(R.string.RegFormLastName),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(lastName = it) })
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showDateOfBirth) {
            PlaceField(
                text = if (formData.dateOfBirth.isNullOrEmpty())
                    stringResource(R.string.RegFormDateOfBirthPlaceholder)
                else
                    formData.dateOfBirth,
                label = stringResource(R.string.RegFormDateOfBirth),
                onClick = { showDateSheet = true }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showGender) {
            PlaceField(
                text = if (formData.gender.isNullOrEmpty())
                    stringResource(R.string.RegFormGender)
                else
                    formData.gender,
                label = stringResource(R.string.LabelGender),
                onClick = { showGenderSheet = true }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showCountry) {
            PlaceField(
                text = formData.country,
                label = stringResource(R.string.CountryLabel),
                onClick = { showCountrySheet = true }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showCity) {
            PlaceField(
                text = formData.city,
                label = stringResource(R.string.CityLabel),
                onClick = { showCitySheet = true }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showSpecialisation) {
//            RegDropdownField(
//                value = formData.specialisation ?: "",
//                placeholder = stringResource(R.string.RegFormSpecialisation),
//                onClick = { onIntent(RegFormsIntent.OnSpecialisationPickerOpen) }
//            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showRole) {
//            RegDropdownField(
//                value = formData.subRole.labelRes.let { stringResource(it) },
//                placeholder = stringResource(R.string.RegFormRole),
//                onClick = { } // уже выбрано на предыдущем шаге
//            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(72.dp))
    }
}

// Конфигурация первого шага — что показывать
data class StepOneConfig(
    @StringRes val titleRes: Int,
    val showFirstName: Boolean = true,
    val showLastName: Boolean = true,
    val showDateOfBirth: Boolean = false,
    val showGender: Boolean = false,
    val showCountry: Boolean = false,
    val showCity: Boolean = false,
    val showCompanyName: Boolean = false,
    val showSpecialisation: Boolean = false,
    val showRole: Boolean = false, // для Booker — поле Role
)

fun SubRole.identityStepConfig(): StepOneConfig = when (this) {
    // Companies (A) — название компании + тип
    SubRole.MODELING_AGENCY,
    SubRole.FASHION_BRAND,
    SubRole.BEAUTY_BRAND,
    SubRole.BRAND_OR_BUSINESS,
    SubRole.EVENT_AGENCY,
    SubRole.MAGAZINE -> StepOneConfig(
        titleRes = R.string.RegFormCompanyInfoTitle,
        showFirstName = false,
        showLastName = false,
        showCompanyName = true,
    )
    // Industry Pros (B) — имя + фамилия + роль
    SubRole.SCOUT,
    SubRole.BOOKER,
    SubRole.CASTING_DIRECTOR,
    SubRole.TALENT_MANAGER -> StepOneConfig(
        titleRes = R.string.RegFormProfessionalIdentityTitle,
        showRole = true,
    )
    // Creative (C.1) — имя + фамилия + специализация
    SubRole.PHOTOGRAPHER,
    SubRole.STYLIST,
    SubRole.MUA,
    SubRole.HAIR_STYLIST,
    SubRole.VIDEOGRAPHER,
    SubRole.CREATIVE_DIRECTOR,
    SubRole.FASHION_DESIGNER -> StepOneConfig(
        titleRes = R.string.RegFormCreativeIdentityTitle,
        showSpecialisation = true,
    )

    // Creative (C.2)
    SubRole.STUDIO -> StepOneConfig(
        titleRes = R.string.RegFormStudioDetailsTitle,
        showFirstName = false,
        showLastName = false,
        showCompanyName = true,
        showCountry = true,
        showCity = true,
    )
    // Talent Model/NewTalent (D1/D2) — имя + фамилия + дата + гендер
    SubRole.MODEL,
    SubRole.NEW_TALENT -> StepOneConfig(
        titleRes = R.string.RegFormYourIdentityTitle,
        showDateOfBirth = true,
        showGender = true,
    )
    // Talent Actor/Dancer/Singer (D3/D4/D5) — то же + специализация
    SubRole.ACTOR,
    SubRole.DANCER,
    SubRole.SINGER -> StepOneConfig(
        titleRes = R.string.RegFormYourIdentityTitle,
        showDateOfBirth = true,
        showGender = true,
        showSpecialisation = true,
    )
    // Fan (E) — имя + фамилия + дата + страна
    SubRole.FAN -> StepOneConfig(
        titleRes = R.string.RegFormFanIdentityTitle,
        showDateOfBirth = true,
        showCountry = true,
    )
}