package org.telegram.divo.common

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    suspend fun loadInitial() {
        if (_state.value.isLoading) return

        Log.d("MyTag", "Paginator: loadInitial start, limit=$limit")
        _state.value = PaginatorState(isLoading = true)

        try {
            val result = onLoad(0, limit)
            Log.d("MyTag", "Paginator: loadInitial success, items=${result.items.size}, totalCount=${result.totalCount}")
            _state.value = PaginatorState(
                items = result.items,
                isLoading = false,
                hasMore = result.items.size < result.totalCount,
                currentOffset = result.items.size
            )
        } catch (e: Exception) {
            Log.d("MyTag", "Paginator: loadInitial error: ${e.message}")
            _state.value = PaginatorState(
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) {
            Log.d("MyTag", "Paginator: loadMore skipped (isLoading=${current.isLoading}, isLoadingMore=${current.isLoadingMore}, hasMore=${current.hasMore})")
            return
        }

        Log.d("MyTag", "Paginator: loadMore start, offset=${current.currentOffset}, limit=$limit")
        _state.value = current.copy(isLoadingMore = true, error = null)

        try {
            val result = onLoad(current.currentOffset, limit)
            val allItems = current.items + result.items
            Log.d("MyTag", "Paginator: loadMore success, newItems=${result.items.size}, total=${allItems.size}, totalCount=${result.totalCount}, hasMore=${allItems.size < result.totalCount}")
            _state.value = current.copy(
                items = allItems,
                isLoadingMore = false,
                hasMore = allItems.size < result.totalCount,
                currentOffset = allItems.size
            )
        } catch (e: Exception) {
            Log.d("MyTag", "Paginator: loadMore error: ${e.message}")
            _state.value = current.copy(
                isLoadingMore = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    fun reset() {
        _state.value = PaginatorState()
    }
}