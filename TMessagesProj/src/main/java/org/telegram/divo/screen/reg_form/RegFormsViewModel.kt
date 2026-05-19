package org.telegram.divo.screen.reg_form

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.screen.search.LocalCity
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader

class RegFormsViewModel : BaseViewModel<RegFormsState, RegFormsIntent, RegFormsEffect>() {

    override fun createInitialState() = RegFormsState()

    override fun handleIntent(intent: RegFormsIntent) {
        when (intent) {
            is RegFormsIntent.Init -> init(intent.subRole)
            is RegFormsIntent.OnFieldChanged -> onFieldChanged(intent.update)
            is RegFormsIntent.OnContinue -> onContinue()
            is RegFormsIntent.OnBack -> onBack()
        }
    }

    private fun init(subRole: SubRole) {
        setState {
            copy(
                steps = subRole.formSteps(),
                formData = RegistrationFormData(subRole = subRole)
            )
        }
        loadCountries()
        loadCities()
    }

    private fun onFieldChanged(update: RegistrationFormData.() -> RegistrationFormData) {
        val current = state.value.formData ?: return
        setState { copy(formData = current.update()) }
    }

    private fun onContinue() {
        logFormData()
        if (state.value.isLastStep) {
            sendEffect(RegFormsEffect.FinishRegistration)
        } else {
            setState { copy(currentStepIndex = currentStepIndex + 1) }
        }
    }

    private fun onBack() {
        if (state.value.currentStepIndex == 0) {
            sendEffect(RegFormsEffect.NavigateBack)
        } else {
            setState { copy(currentStepIndex = currentStepIndex - 1) }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<LocalCountry>()
            try {
                val stream = ApplicationLoader.applicationContext.assets.open("countries.txt")
                val reader = BufferedReader(InputStreamReader(stream))
                reader.forEachLine { line ->
                    val args = line.split(";")
                    if (args.size >= 3) {
                        val code = args[0]
                        val shortname = args[1]
                        val name = args[2]
                        val flag = LocaleController.getLanguageFlag(shortname)
                        list.add(
                            LocalCountry(
                                code = code,
                                shortName = shortname,
                                name = name,
                                flag = flag
                            )
                        )
                    }
                }
                reader.close()
                stream.close()

                list.sortBy { it.name }

                setState { copy(allCountries = list) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCities() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cities = mutableListOf<LocalCity>()
                val stream = ApplicationLoader.applicationContext.assets.open("cities.txt")
                stream.bufferedReader().forEachLine { line ->
                    val cols = line.split("\t")
                    if (cols.size > 14) {
                        cities.add(
                            LocalCity(
                                id = cols[0].toLongOrNull() ?: return@forEachLine,
                                name = cols[1],
                                asciiName = cols[2],
                                countryCode = cols[8],
                                population = cols[14].toIntOrNull() ?: 0
                            )
                        )
                    }
                }
                stream.close()
                setState { copy(allCities = cities) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logFormData() {
        val data = state.value.formData ?: return
        android.util.Log.d("RegForm", buildString {
            appendLine("=== REGISTRATION FORM DATA ===")
            appendLine("SubRole: ${data.subRole}")
            appendLine("--- Common ---")
            appendLine("First name: ${data.firstName}")
            appendLine("Last name: ${data.lastName}")
            appendLine("Date of birth: ${data.dateOfBirth}")
            appendLine("Gender: ${data.gender}")
            appendLine("Country: ${data.country}")
            appendLine("City: ${data.city}")
            appendLine("--- Company ---")
            appendLine("Company name: ${data.companyName}")
            appendLine("Website URL: ${data.websiteUrl}")
            appendLine("Contact role: ${data.contactRole}")
            appendLine("Contact name: ${data.contactName}")
            appendLine("Contact phone: ${data.contactPhone}")
            appendLine("--- Creative / Professional ---")
            appendLine("Specialisation: ${data.specialisation}")
            appendLine("Instagram URL: ${data.instagramUrl}")
            appendLine("Portfolio URL: ${data.portfolioUrl}")
            appendLine("Agency name: ${data.agencyName}")
            appendLine("--- Talent ---")
            appendLine("Showreel URL: ${data.showreelUrl}")
            appendLine("Casting profile URL: ${data.castingProfileUrl}")
            appendLine("--- Photo ---")
            appendLine("Photo URI: ${data.photoUri}")
            appendLine("==============================")
        })
    }
}