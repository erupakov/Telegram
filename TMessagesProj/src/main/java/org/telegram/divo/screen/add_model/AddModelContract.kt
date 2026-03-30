package org.telegram.divo.screen.add_model

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState

data class LocalCountry(
    val code: String,
    val shortName: String,
    val name: String,
    val flag: String?
)

data class State(
    val name: String = "",
    val avatarUri: String? = null,
    val link: String = "",
    val country: LocalCountry? = null,
    val countries: List<LocalCountry> = emptyList(),
    val isLoading: Boolean = false,
) : ViewState

sealed class Intent : ViewIntent {
    data object OnNextClicked : Intent()

    data class OnNicknameChanged(
        val name: String
    ) : Intent()

    data class OnAvatarSelected(
        val uri: String
    ) : Intent()

    data class OnCountrySelected(
        val country: LocalCountry
    ) : Intent()

    data class OnLinkChanged(
        val link: String
    ) : Intent()

    data object OnBack : Intent()
}

sealed class Effect : ViewEffect {
    data object NavigateBack : Effect()
    data object NavigateNext : Effect()
}