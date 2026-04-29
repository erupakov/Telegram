package org.telegram.divo.usecase

import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.dal.repository.UserActionEvent

/**
 * Универсальный UseCase для переключения закладки (bookmark/favorite) пользователя.
 * Реализует оптимистичное обновление с откатом при ошибке.
 *
 * @param entity строковый тип сущности ("model", "agency", "new_face" и т.д.)
 * @param onUpdate вызывается сразу с новым состоянием (оптимистично)
 * @param onRollback вызывается если API вернул ошибку — для отката UI
 * @param onSuccess вызывается при успехе — для показа снэкбара и т.п.
 * @param onError вызывается при ошибке — для показа ошибки
 */
class ToggleBookmarkUseCase(
    private val repository: PublicationRepository = DivoApi.publicationRepository,
) {
    suspend fun execute(
        userId: Int,
        entity: String,
        isFavorite: Boolean,
        currentFollowersCount: Int,
        onUpdate: (newFavorite: Boolean, newFollowersCount: Int) -> Unit,
        onRollback: () -> Unit,
        onSuccess: (newFavorite: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        val newFavorite = !isFavorite
        val newCount = if (newFavorite) currentFollowersCount + 1
                       else (currentFollowersCount - 1).coerceAtLeast(0)

        onUpdate(newFavorite, newCount)

        val result = if (newFavorite) {
            repository.markFavorite(userId, entity)
        } else {
            repository.unmarkFavorite(userId, entity)
        }

        if (result is DivoResult.Success) {
            repository.emitEvent(
                UserActionEvent.BookmarkChanged(
                    userId = userId,
                    isFavorite = newFavorite,
                    newFollowersCount = newCount
                )
            )
            onSuccess(newFavorite)
        } else {
            onRollback()
            onError(result.getErrorMessage())
        }
    }
}
