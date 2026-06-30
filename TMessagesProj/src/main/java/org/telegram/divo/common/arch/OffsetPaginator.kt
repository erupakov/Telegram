package org.telegram.divo.common.arch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.cancellation.CancellationException

data class PaginatedResult<T>(
    val items: List<T>,
    val totalCount: Int
)

data class PaginatorState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val currentOffset: Int = 0
)

class OffsetPaginator<T>(
    private val limit: Int = 10,
    private val onLoad: suspend (offset: Int, limit: Int) -> PaginatedResult<T>
) {

    private val _state = MutableStateFlow(PaginatorState<T>())
    val state: StateFlow<PaginatorState<T>> = _state.asStateFlow()

    suspend fun loadInitial(clearItems: Boolean = true) {
        if (_state.value.isLoading) return

        _state.value = _state.value.copy(
            isLoading = true,
            items = if (clearItems) emptyList() else _state.value.items,
            error = null
        )

        try {
            val result = onLoad(0, limit)
            _state.value = _state.value.copy(
                items = result.items,
                isLoading = false,
                hasMore = result.items.size < result.totalCount,
                currentOffset = result.items.size
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) {
            return
        }
        _state.value = current.copy(isLoadingMore = true, error = null)

        try {
            val result = onLoad(current.currentOffset, limit)
            val allItems = current.items + result.items

            _state.value = current.copy(
                items = allItems,
                isLoadingMore = false,
                hasMore = allItems.size < result.totalCount,
                currentOffset = allItems.size
            )
        } catch (e: Exception) {
            _state.value = current.copy(
                isLoadingMore = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    fun reset() {
        _state.value = PaginatorState()
    }

    fun updateItem(predicate: (T) -> Boolean, updater: (T) -> T) {
        val current = _state.value
        val newItems = current.items.map { if (predicate(it)) updater(it) else it }
        _state.value = current.copy(items = newItems)
    }
}