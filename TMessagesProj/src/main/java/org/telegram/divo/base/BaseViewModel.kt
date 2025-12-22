package org.telegram.divo.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface ViewState

interface ViewIntent

interface ViewEffect

abstract class BaseViewModel<S : ViewState, I : ViewIntent, A : ViewEffect> : ViewModel() {

    private val initialState: S by lazy { createInitialState() }
    abstract fun createInitialState(): S

    private val _state: MutableStateFlow<S> by lazy { MutableStateFlow(initialState) }
    val state: StateFlow<S> by lazy { _state.asStateFlow() }

    private val _intent: Channel<I> = Channel(Channel.UNLIMITED)

    private val _effect: Channel<A> = Channel(Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToIntents()
    }

    fun setIntent(intent: I) {
        viewModelScope.launch {
            _intent.send(intent)
        }
    }

    protected fun sendEffect(effect: A) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    protected fun setState(reducer: S.() -> S) {
        val newState = state.value.reducer()
        _state.value = newState
    }

    private fun subscribeToIntents() {
        viewModelScope.launch {
            _intent.receiveAsFlow().collect {
                handleIntent(it)
            }
        }
    }

    abstract fun handleIntent(intent: I)
}
