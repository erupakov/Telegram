package org.telegram.divo.screen.event_filter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.telegram.tgnet.TLRPC

object EventFilterStore {

    private val _filter = MutableStateFlow<TLRPC.TL_event_filter?>(null)
    val filter: StateFlow<TLRPC.TL_event_filter?> = _filter.asStateFlow()

    /** Set (save) filter */
    fun set(value: TLRPC.TL_event_filter?) {
        _filter.value = value
    }

    /** Clear filter */
    fun clear() {
        _filter.value = null
    }

    /** Read current value */
    fun get(): TLRPC.TL_event_filter? = _filter.value

    /** Update helper */
    fun update(block: (TLRPC.TL_event_filter?) -> TLRPC.TL_event_filter?) {
        _filter.update(block)
    }
}
