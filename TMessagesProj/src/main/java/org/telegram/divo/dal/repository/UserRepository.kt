package org.telegram.divo.dal.repository

import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.toEntities
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.AgencyModels
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserInfo

class UserRepository(
    private val service: UserService
) {

    suspend fun getCurrentUserInfo(): DivoResult<UserInfo> = resultOf {
        service.getCurrentUserInfo().toEntity()
    }

    suspend fun getUserById(userId: Int): DivoResult<UserInfo> = resultOf {
        service.getUserById(userId).toEntity()
    }

    suspend fun getUserGalleryList(
        userId: Int,
        offset: Int = 0,
        limit: Int = 10,
    ): DivoResult<List<UserGalleryItem>> = resultOf {
        service.getUserGalleryList(
            request = UserGalleryListRequest(
                userId = userId,
                offset = offset,
                limit = limit
            )
        ).toEntities()
    }

    suspend fun getAgencyModels(
        agencyId: Int = 1,
        offset: Int = 0,
        limit: Int = 10,
    ): DivoResult<AgencyModels> = resultOf {
        service.getAgencyModels(
            agencyId = agencyId,
            request = AgencyModelsRequest(offset, limit)
        ).toEntities()
    }
}

