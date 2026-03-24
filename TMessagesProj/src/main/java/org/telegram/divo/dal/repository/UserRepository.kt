package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.dal.dto.common.toDto
import org.telegram.divo.dal.dto.common.toEntities
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.dal.dto.user.AddGalleryRequest
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.UpdateProfileRequest
import org.telegram.divo.dal.dto.user.UpsertSocialNetworkRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.toDto
import org.telegram.divo.dal.dto.user.toEntities
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.AgencyModels
import org.telegram.divo.entity.Engagement
import org.telegram.divo.entity.UploadedFile
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserGalleryList
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.entity.UserSocialNetwork
import java.io.File
import java.util.TimeZone

private const val MAX_CACHED_USERS = 5

class UserRepository(
    private val service: UserService
) {
    private val _currentUserCache = MutableStateFlow<UserInfo?>(null)
    val currentUserFlow: StateFlow<UserInfo?> = _currentUserCache.asStateFlow()

    private val _galleryCache = MutableStateFlow<Map<Int, UserGalleryList>>(emptyMap())

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
                gender = userInfo.gender?.id,
                birthday = userInfo.birthday,
                geoCityId = userInfo.city?.id,
                measuringSystem = userInfo.measuringSystem,
                subrole = userInfo.subrole,
                pushNotifications = userInfo.pushNotifications,
                isRegistrationFinished = userInfo.isRegistrationFinished,
                photo = UuidContainerDto(userInfo.photoUuid),
                avatar = UuidContainerDto(userInfo.avatarUuid),
                model = userInfo.model?.toDto(),
                agency = userInfo.agency?.toDto(),
                customer = userInfo.customer?.toDto()
            )
        )
            .toEntity()
            .also { _currentUserCache.value = it }
    }

    suspend fun updateAgency(
        agency: Agency
    ): DivoResult<Unit> = resultOf {
        service.updateAgency(
            agency.toDto()
        )

        _currentUserCache.value = service.getCurrentUserInfo().toEntity()
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

    suspend fun upsertSocialNetwork(socialNetworkId: Int, nickname: String): DivoResult<Unit> = resultOf {
        service.upsertSocialNetwork(
            UpsertSocialNetworkRequest(socialNetworkId, nickname)
        )
        _currentUserCache.value = service.getCurrentUserInfo().toEntity()
    }

    fun galleryFlow(userId: Int): Flow<UserGalleryList?> =
        _galleryCache.map { it[userId] }

    fun getGalleryCache(userId: Int): UserGalleryList? =
        _galleryCache.value[userId]

    suspend fun getUserGalleryList(
        userId: Int,
        offset: Int,
        limit: Int,
    ): DivoResult<UserGalleryList> = resultOf {
        if (offset == 0) {
            _galleryCache.value[userId]?.let { return@resultOf it }
        }

        service.getUserGalleryList(
            request = UserGalleryListRequest(
                userId = userId,
                offset = offset,
                limit = limit
            )
        ).toEntities().also { newPage ->
            updateGalleryCache(userId, newPage, offset)
        }
    }

    suspend fun addToGallery(uuid: String): DivoResult<UserGalleryItem> = resultOf {
        service.addToGallery(AddGalleryRequest(uuid)).data.toEntity().also { newItem ->
            _currentUserCache.value?.id?.let { userId ->
                _galleryCache.update { cache ->
                    val existing = cache[userId]
                    if (existing != null) {
                        cache + (userId to existing.copy(
                            items = listOf(newItem) + existing.items
                        ))
                    } else cache
                }
            }
        }
    }

    suspend fun deleteFromGallery(id: Int): DivoResult<Unit> = resultOf {
        service.deleteFromGallery(id)
        _currentUserCache.value?.id?.let { userId ->
            _galleryCache.update { cache ->
                val existing = cache[userId] ?: return@update cache
                cache + (userId to existing.copy(
                    items = existing.items.filter { it.id != id }
                ))
            }
        }
    }

    suspend fun uploadPhoto(file: File): DivoResult<UploadedFile> = resultOf {
        service.uploadFile(file.toMultipart()).toEntity()
    }

    suspend fun getUserSocialNetworks(): DivoResult<List<UserSocialNetwork>> = resultOf {
        service.getUserSocialNetworks().toEntities()
    }

    suspend fun getEngagement(userId: Int, offset: Int, limit: Int): DivoResult<Engagement> = resultOf {
        service.getEngagement(userId = userId, offset = offset, limit = limit).toEntity()
    }

    private fun File.toMultipart(): MultipartBody.Part {
        val requestBody = asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData(
            name = "file",
            filename = name,
            body = requestBody
        )
    }

    private fun updateGalleryCache(userId: Int, newPage: UserGalleryList, offset: Int) {
        _galleryCache.update { cache ->
            val existing = cache[userId]

            val merged = if (existing == null || offset == 0) {
                newPage
            } else {
                existing.copy(
                    items = (existing.items + newPage.items).distinctBy { it.id },
                    pagination = newPage.pagination
                )
            }

            val updated = cache + (userId to merged)

            if (updated.size > MAX_CACHED_USERS) {
                updated.entries.drop(1).associate { it.key to it.value }
            } else {
                updated
            }
        }
    }
}

