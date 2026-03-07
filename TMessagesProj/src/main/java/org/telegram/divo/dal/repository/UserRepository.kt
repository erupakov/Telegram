package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.dal.dto.common.toEntities
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.dal.dto.user.AddGalleryRequest
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.UpdateProfileRequest
import org.telegram.divo.dal.dto.user.UpsertSocialNetworkRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.toEntities
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.dal.dto.user.toDto
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.AgencyModels
import org.telegram.divo.entity.UploadedFile
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserGalleryList
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.entity.UserSocialNetwork
import java.io.File
import java.util.TimeZone

class UserRepository(
    private val service: UserService
) {

    private val _currentUserCache = MutableStateFlow<UserInfo?>(null)
    val currentUserFlow: StateFlow<UserInfo?> = _currentUserCache.asStateFlow()

    suspend fun getCurrentUserInfo(forceRefresh: Boolean = false): DivoResult<UserInfo> = resultOf {
        if (!forceRefresh) {
            _currentUserCache.value?.let { return@resultOf it }
        }
        service.getCurrentUserInfo().toEntity().also { _currentUserCache.value = it }
    }

    suspend fun getUserById(userId: Int): DivoResult<UserInfo> = resultOf {
        service.getUserById(userId).toEntity()
    }

    suspend fun updateProfile(
        userInfo: UserInfo
    ): DivoResult<UserInfo> = resultOf {
        service.updateProfile(
            UpdateProfileRequest(
                fullName = userInfo.fullName,
                phone = userInfo.phone,
                timezone = TimeZone.getDefault().id,
                gender = userInfo.gender.id,
                birthday = userInfo.birthday,
                geoCityId = userInfo.city.id,
                measuringSystem = userInfo.measuringSystem,
                subrole = userInfo.subrole,
                pushNotifications = userInfo.pushNotifications,
                isRegistrationFinished = userInfo.isRegistrationFinished,
                photo = UuidContainerDto(userInfo.photoUuid),
                avatar = UuidContainerDto(userInfo.avatarUuid),
                model = userInfo.model.toDto(),
                customer = userInfo.customer?.toDto()
            )
        )
            .toEntity()
            .also { _currentUserCache.value = it }
    }

    suspend fun getUserGalleryList(
        userId: Int,
        offset: Int,
        limit: Int,
    ): DivoResult<UserGalleryList> = resultOf {
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
        _currentUserCache.value = service.getCurrentUserInfo().toEntity()
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

