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
        isFollowed: Boolean,
        currentFollowersCount: Int,
        onUpdate: (newFollowed: Boolean, newFollowersCount: Int) -> Unit,
        onRollback: () -> Unit,
        onSuccess: (newFollowed: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        val newFollowed = !isFollowed
        val newCount = if (newFollowed) currentFollowersCount + 1
                       else (currentFollowersCount - 1).coerceAtLeast(0)

        onUpdate(newFollowed, newCount)

        val result = if (newFollowed) {
            repository.markFavorite(userId)
        } else {
            repository.unmarkFavorite(userId)
        }

        if (result is DivoResult.Success) {
            repository.emitEvent(
                UserActionEvent.BookmarkChanged(
                    userId = userId,
                    isFavorite = newFollowed,
                    newFollowersCount = newCount
                )
            )
            onSuccess(newFollowed)
        } else {
            onRollback()
            onError(result.getErrorMessage())
        }
    }
}
