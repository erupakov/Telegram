package org.telegram.divo.dal.dao

import org.telegram.divo.dal.DivoResult
import org.telegram.divo.dal.safeCall
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dto.user.UserInfoResponse

class UserDao(
    private val service: UserService
) {

    suspend fun getCurrentUserInfo(): DivoResult<UserInfoResponse> {
        return safeCall { service.getCurrentUserInfo() }
    }

    suspend fun updateProfile(fields: Map<String, Any?>): DivoResult<UserInfoResponse> {
        return safeCall { service.updateProfile(fields) }
    }
}

