package org.telegram.divo.usecase

import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.dal.repository.UserActionEvent

/**
 * Универсальный UseCase для переключения лайка на публикации.
 * Реализует оптимистичное обновление с откатом при ошибке.
 *
 * @param onUpdate вызывается сразу с новым состоянием (оптимистично)
 * @param onRollback вызывается если API вернул ошибку — для отката UI
 * @param onSuccess вызывается при успехе — для показа снэкбара и т.п.
 * @param onError вызывается при ошибке — для показа ошибки
 */
class ToggleLikeUseCase(
    private val repository: PublicationRepository = DivoApi.publicationRepository,
) {
    suspend fun execute(
        userId: Int,
        isLiked: Boolean,
        currentCount: Int,
        onUpdate: (newLiked: Boolean, newCount: Int) -> Unit,
        onRollback: () -> Unit,
        onSuccess: (newLiked: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        val newLiked = !isLiked
        val newCount = if (isLiked) (currentCount - 1).coerceAtLeast(0) else currentCount + 1

        onUpdate(newLiked, newCount)

        val result = if (newLiked) {
            repository.likePost(userId)
        } else {
            repository.unlikePost(userId)
        }

        if (result is DivoResult.Success) {
            repository.emitEvent(
                UserActionEvent.LikeChanged(
                    userId = userId,
                    isLiked = newLiked,
                    newLikesCount = newCount
                )
            )
            onSuccess(newLiked)
        } else {
            onRollback()
            onError(result.getErrorMessage())
        }
    }
}
