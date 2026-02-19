package org.telegram.divo.dal.repository

import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.user.UserInfoResponse
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.entity.UserInfo

class UserRepository(
    private val service: UserService
) {

    suspend fun getCurrentUserInfo(): DivoResult<UserInfo> = resultOf {
        service.getCurrentUserInfo().toEntity()
    }

    suspend fun updateProfile(fields: Map<String, Any?>): DivoResult<UserInfoResponse> {
        return resultOf { service.updateProfile(fields) }
    }
}

