package org.telegram.divo.screen.add_model

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi

class AddModelViewModel : BaseViewModel<State, Intent, Effect>() {

    private val locationRepository = DivoApi.locationRepository

    init {
        loadCountries()
    }

    override fun createInitialState(): State = State()

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.OnCountrySelected -> setState { copy(country = intent.country) }
            is Intent.OnLinkChanged -> setState { copy(link = intent.link) }
            Intent.OnNextClicked -> sendEffect(Effect.NavigateNext)
            is Intent.OnNicknameChanged -> setState { copy(name = intent.name) }
            is Intent.OnAvatarSelected -> setState { copy(avatarUri = intent.uri) }
            Intent.OnBack -> sendEffect(Effect.NavigateBack)
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            val list = locationRepository.getCountries()
            setState { copy(countries = list) }
        }
    }
}
