package org.telegram.divo.dal.repository

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.common.UserSocialNetworkDto
import org.telegram.divo.dal.dto.common.toEntities
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.dal.dto.user.AddGalleryRequest
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.UpsertSocialNetworkRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.toEntities
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.AgencyModels
import org.telegram.divo.entity.UploadedFile
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.entity.UserSocialNetwork
import java.io.File

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

    suspend fun uploadPhoto(file: File): DivoResult<UploadedFile> = resultOf {
        service.uploadFile(file.toMultipart()).toEntity()
    }

    suspend fun addToGallery(uuid: String): DivoResult<UserGalleryItem> = resultOf {
        service.addToGallery(AddGalleryRequest(uuid)).data.toEntity()
    }

    suspend fun upsertSocialNetwork(socialNetworkId: Int, nickname: String): DivoResult<Unit> = resultOf {
        service.upsertSocialNetwork(
            UpsertSocialNetworkRequest(socialNetworkId, nickname)
        )
    }

    suspend fun getUserSocialNetworks(): DivoResult<List<UserSocialNetwork>> = resultOf {
        service.getUserSocialNetworks().toEntities()
    }

    private fun File.toMultipart(): MultipartBody.Part {
        val requestBody = asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData(
            name = "file",
            filename = name,
            body = requestBody
        )
    }
}

