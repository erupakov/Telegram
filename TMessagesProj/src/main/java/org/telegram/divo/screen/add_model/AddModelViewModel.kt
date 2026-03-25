package org.telegram.divo.screen.add_model

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader

class AddModelViewModel : BaseViewModel<State, Intent, Effect>() {
    
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
                
                setState { copy(countries = list) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}