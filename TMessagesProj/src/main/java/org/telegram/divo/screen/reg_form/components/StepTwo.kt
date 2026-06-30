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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.bottomsheets.CountryPickerSheet
import org.telegram.divo.components.inputs.RegTextField
import org.telegram.divo.components.bottomsheets.ParameterBottomSheet
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.screen.reg_form.RegFormsIntent
import org.telegram.divo.screen.reg_form.RegFormsState
import org.telegram.divo.screen.reg_form.RegistrationFormData
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.components.bottomsheets.CityPickerSheet
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTwo(
    formData: RegistrationFormData,
    state: RegFormsState,
    onIntent: (RegFormsIntent) -> Unit,
) {
    val config = formData.subRole.stepTwoConfig()
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var showCitySheet by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showGenderSheet by rememberSaveable { mutableStateOf(false) }
    var showSpecialisationSheet by rememberSaveable { mutableStateOf(false) }

    if (showCountrySheet) {
        CountryPickerSheet(
            list = state.allCountries,
            selectedCountries = listOfNotNull(
                state.allCountries.find { it.name == formData.country }
            ),
            onDismiss = { showCountrySheet = false },
            onPick = { selectedList ->
                val country = selectedList.firstOrNull()
                onIntent(RegFormsIntent.OnFieldChanged {
                    copy(
                        country = country?.name ?: "", 
                        countryCode = country?.shortName ?: "", 
                        city = null
                    )
                })
                showCountrySheet = false
            }
        )
    }

    if (showCitySheet) {
        CityPickerSheet(
            list = state.allCities,
            selectedCity = formData.city,
            allCountries = state.allCountries,
            selectedCountries = listOfNotNull(
                state.allCountries.find { it.name == formData.country }
            ),
            onDismiss = { showCitySheet = false },
            onPick = { selectedCity ->
                val matchedCountry = state.allCountries.find {
                    it.shortName.equals(selectedCity?.countryCode, ignoreCase = true)
                }
                onIntent(RegFormsIntent.OnFieldChanged {
                    copy(
                        city = selectedCity,
                        country = if (country.isEmpty() && matchedCountry != null)
                            matchedCountry.name
                        else
                            country,
                        countryCode = if (countryCode.isEmpty() && matchedCountry != null)
                            matchedCountry.shortName
                        else
                            countryCode
                    )
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
            onSave = {
                onIntent(RegFormsIntent.OnFieldChanged { copy(dateOfBirth = it) })
                showDateSheet = false
            },
            onDelete = { showDateSheet = false }
        )
    }

    if (showGenderSheet) {
        ParameterBottomSheet(
            paramType = ParametersType.GENDER,
            options = stringArrayResource(R.array.GenderItems).toList(),
            initialValue = formData.gender.orEmpty(),
            onDismiss = { showGenderSheet = false },
            onSave = {
                onIntent(RegFormsIntent.OnFieldChanged { copy(gender = it) })
                showGenderSheet = false
            },
            onDelete = { showGenderSheet = false }
        )
    }

    if (showSpecialisationSheet) {
        ParameterBottomSheet(
            paramType = ParametersType.SPECIALISATION,
            options = formData.subRole.specialisationOptions(),
            initialValue = formData.specialisation.orEmpty(),
            onDismiss = { showSpecialisationSheet = false },
            onSave = {
                onIntent(RegFormsIntent.OnFieldChanged { copy(specialisation = it) })
                showSpecialisationSheet = false
            },
            onDelete = { showSpecialisationSheet = false }
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
                text = formData.city?.let { it.matchedName ?: it.name }.orEmpty(),
                label = stringResource(R.string.CityLabel),
                onClick = { showCitySheet = true }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showWebsiteOrInstagram) {
            RegTextField(
                value = formData.instagramUrl,
                placeholder = stringResource(R.string.RegFormWebsiteOrInstagram),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(instagramUrl = it) })
                }
            )
            if (formData.subRole == SubRole.STUDIO) TextHint(stringResource(R.string.RegFormWebsiteOrInstagramHint))
            Spacer(Modifier.height(16.dp))
        }

        if (config.showContactName) {
            RegTextField(
                value = formData.contactName,
                placeholder = stringResource(R.string.RegFormContactName),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(contactName = it) })
                }
            )
            if (formData.subRole == SubRole.STUDIO) TextHint(stringResource(R.string.RegFormContactNameHint))
            Spacer(Modifier.height(16.dp))
        }

        if (config.showContactPhone) {
            RegTextField(
                value = formData.contactPhone,
                placeholder = stringResource(R.string.RegFormContactPhone),
                keyboardType = KeyboardType.Phone,
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(contactPhone = it) })
                }
            )
            if (formData.subRole == SubRole.STUDIO) TextHint(stringResource(R.string.RegFormContactPhoneHint))
            Spacer(Modifier.height(16.dp))
        }

        if (config.showSpecialisation) {
            PlaceField(
                text = if (formData.specialisation.isNullOrEmpty())
                    stringResource(R.string.RegFormSpecialisation)
                else formData.specialisation,
                label = stringResource(R.string.RegFormSpecialisationLabel),
                onClick = { showSpecialisationSheet = true }
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}

data class StepTwoConfig(
    @StringRes val titleRes: Int,
    // Location fields
    val showCountry: Boolean = false,
    val showCity: Boolean = false,
    // Personal details fields
    val showDateOfBirth: Boolean = false,
    val showGender: Boolean = false,
    // Contact fields (Studio)
    val showWebsiteOrInstagram: Boolean = false,
    val showContactName: Boolean = false,
    val showContactPhone: Boolean = false,
    // Specialisation (Actor/Dancer/Singer)
    val showSpecialisation: Boolean = false,
)

fun SubRole.stepTwoConfig(): StepTwoConfig = when (this) {
    // Companies (A) — Where are you based?
    SubRole.MODELING_AGENCY,
    SubRole.FASHION_BRAND,
    SubRole.BEAUTY_BRAND,
    SubRole.BRAND_OR_BUSINESS,
    SubRole.EVENT_AGENCY,
    SubRole.MAGAZINE -> StepTwoConfig(
        titleRes = R.string.RegFormWhereAreYouBased,
        showCountry = true,
        showCity = true,
    )
    // Industry Pros (B) + Creative C1 — Personal details
    SubRole.SCOUT,
    SubRole.BOOKER,
    SubRole.CASTING_DIRECTOR,
    SubRole.TALENT_MANAGER,
    SubRole.PHOTOGRAPHER,
    SubRole.STYLIST,
    SubRole.MUA,
    SubRole.HAIR_STYLIST,
    SubRole.VIDEOGRAPHER,
    SubRole.CREATIVE_DIRECTOR,
    SubRole.FASHION_DESIGNER -> StepTwoConfig(
        titleRes = R.string.RegFormPersonalDetails,
        showDateOfBirth = true,
        showGender = true,
        showCountry = true,
        showCity = true,
    )
    // Studio C2.1 — Contact details
    SubRole.STUDIO -> StepTwoConfig(
        titleRes = R.string.RegFormContactDetails,
        showWebsiteOrInstagram = true,
        showContactName = true,
        showContactPhone = true,
    )
    // Talent Model/NewTalent — Location
    SubRole.MODEL,
    SubRole.NEW_TALENT -> StepTwoConfig(
        titleRes = R.string.RegFormLocation,
        showCountry = true,
        showCity = true,
    )
    // Talent Actor/Dancer/Singer — Location + Specialisation
    SubRole.ACTOR,
    SubRole.DANCER,
    SubRole.SINGER -> StepTwoConfig(
        titleRes = R.string.RegFormLocation,
        showCountry = true,
        showCity = true,
        showSpecialisation = true,
    )

    SubRole.FAN -> StepTwoConfig(titleRes = R.string.RegFormLocation)
}

@Composable
private fun SubRole.specialisationOptions(): List<String> = when (this) {
    SubRole.ACTOR -> stringArrayResource(R.array.ActorSpecialisations).toList()
    SubRole.DANCER -> stringArrayResource(R.array.DancerSpecialisations).toList()
    SubRole.SINGER -> stringArrayResource(R.array.SingerSpecialisations).toList()
    else -> emptyList()
}