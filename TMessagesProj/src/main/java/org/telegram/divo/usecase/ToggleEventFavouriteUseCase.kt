package org.telegram.divo.usecase

import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.EventRepository

/**
 * Универсальный UseCase для добавления эвента в избранное.
 * Реализует оптимистичное обновление с откатом при ошибке.
 */
class ToggleEventFavouriteUseCase(
    private val repository: EventRepository = DivoApi.eventRepository,
) {
    suspend fun execute(
        eventId: Int,
        isFavourite: Boolean,
        currentCount: Int,
        onUpdate: (newFavourite: Boolean, newCount: Int) -> Unit,
        onRollback: () -> Unit,
        onSuccess: (newFavourite: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        val newFavourite = !isFavourite
        val newCount = if (isFavourite) (currentCount - 1).coerceAtLeast(0) else currentCount + 1

        onUpdate(newFavourite, newCount)

        val result = if (newFavourite) {
            repository.setFavourite(eventId)
        } else {
            repository.dropFavourite(eventId)
        }

        if (result is DivoResult.Success) {
            onSuccess(newFavourite)
        } else {
            onRollback()
            onError(result.getErrorMessage())
        }
    }
}
