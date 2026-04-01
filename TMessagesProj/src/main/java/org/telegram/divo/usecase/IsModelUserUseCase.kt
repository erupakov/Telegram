package org.telegram.divo.usecase

import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.map
import org.telegram.divo.dal.repository.UserRepository

class IsModelUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): DivoResult<Boolean> =
        userRepository.getCurrentUserInfo().map { it.role.isModel() }
}