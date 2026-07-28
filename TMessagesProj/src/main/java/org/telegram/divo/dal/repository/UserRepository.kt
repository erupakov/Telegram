package org.telegram.divo.dal.repository

import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.PaginatedResult
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.dal.dto.common.toDto
import org.telegram.divo.dal.dto.common.toEntities
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.dal.dto.user.AddGalleryRequest
import org.telegram.divo.dal.dto.user.AgencyModelsRequest
import org.telegram.divo.dal.dto.user.ReportProfileRequest
import org.telegram.divo.dal.dto.user.UpdateProfileRequest
import org.telegram.divo.dal.dto.user.UpsertSocialNetworkRequest
import org.telegram.divo.dal.dto.user.UserGalleryListRequest
import org.telegram.divo.dal.dto.user.toDto
import org.telegram.divo.dal.dto.user.toEntities
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.AgencyModels
import org.telegram.divo.entity.AgencySearchModel
import org.telegram.divo.entity.Appearances
import org.telegram.divo.entity.Engagement
import org.telegram.divo.entity.UploadedFile
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserGalleryList
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.entity.UserSocialNetwork
import org.telegram.messenger.NotificationCenter
import java.io.File
import java.util.TimeZone

private const val MAX_CACHED_USERS = 5

class UserRepository(
    private val service: UserService,
    private val prefs: android.content.SharedPreferences,
    private val accountIndex: Int
) : NotificationCenter.NotificationCenterDelegate {
    private companion object {
        const val KEY_AVATAR_URL = "cached_avatar_url"
        const val KEY_USER_ID = "cached_user_id"
    }

    private val _currentUserCache = MutableStateFlow<UserInfo?>(null)
    val currentUserFlow: StateFlow<UserInfo?> = _currentUserCache.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val savedId = prefs.getInt(KEY_USER_ID, 0)
        val savedUrl = prefs.getString(KEY_AVATAR_URL, null)
        if (savedId != 0 && savedUrl != null) {
            _currentUserCache.value = UserInfo(id = savedId, avatarUrl = savedUrl)
        }
        org.telegram.messenger.AndroidUtilities.runOnUIThread {
            NotificationCenter.getInstance(accountIndex).addObserver(this, NotificationCenter.dialogDeleted)
        }
    }

    private val pendingDeletions = mutableSetOf<Int>()

    private val _galleryCache = MutableStateFlow<Map<Int, UserGalleryList>>(emptyMap())

    suspend fun getCurrentUserInfo(forceRefresh: Boolean = false): DivoResult<UserInfo> = resultOf {
        if (!forceRefresh) {
            _currentUserCache.value?.let { 
                if (it.fullName.isNotEmpty()) return@resultOf it
            }
        }
        val cachedId = _currentUserCache.value?.id?.takeIf { it > 0 } ?: prefs.getInt(KEY_USER_ID, 0)
        
        if (cachedId > 0) {
            coroutineScope {
                val userInfoDeferred = async { service.getCurrentUserInfo() }
                val channelsDeferred = async {
                    try { 
                        val entities = service.getChannels(cachedId).data?.items?.toEntities() ?: emptyList()
                        entities.filter { it.id !in pendingDeletions }
                    } catch (e: Exception) { emptyList() }
                }
                userInfoDeferred.await().toEntity(channelsDeferred.await()).also { updateCacheAndPersist(it) }
            }
        } else {
            val userInfo = service.getCurrentUserInfo()
            val channels = try { 
                val entities = service.getChannels(userInfo.data.id).data?.items?.toEntities() ?: emptyList()
                entities.filter { it.id !in pendingDeletions }
            } catch (e: Exception) { emptyList() }
            userInfo.toEntity(channels).also { updateCacheAndPersist(it) }
        }
    }

    suspend fun getUserById(userId: Int): DivoResult<UserInfo> = resultOf {
        coroutineScope {
            val userInfoDeferred = async { service.getUserById(userId) }
            val channelsDeferred = async {
                try { 
                    val entities = service.getChannels(userId).data?.items?.toEntities() ?: emptyList()
                    entities.filter { it.id !in pendingDeletions }
                } catch (e: Exception) { emptyList() }
            }
            userInfoDeferred.await().toEntity(channelsDeferred.await())
        }
    }

    suspend fun addChannel(telegramChatId: Long, username: String?, inviteLink: String?): DivoResult<Unit> = resultOf {
        service.addChannel(
            org.telegram.divo.dal.dto.user.AddChannelRequest(
                telegramChatId = telegramChatId,
                username = username,
                inviteLink = inviteLink
            )
        )
        getCurrentUserInfo(forceRefresh = true)
    }

    suspend fun deleteChannel(channelId: Int): DivoResult<Unit> = resultOf {
        pendingDeletions.add(channelId)
        try {
            service.deleteChannel(channelId)
        } finally {
            pendingDeletions.remove(channelId)
            getCurrentUserInfo(forceRefresh = true)
        }
    }

    suspend fun updateProfile(
        userInfo: UserInfo
    ): DivoResult<UserInfo> = resultOf {
        var resolvedCityId = userInfo.city?.id?.takeIf { it > 0 }
        if (resolvedCityId == null && userInfo.city?.name?.isNotBlank() == true) {
            try {
                val geoResponse = DivoApi.geoService.searchByAddressName(userInfo.city.name)
                resolvedCityId = geoResponse.data?.firstOrNull()?.city?.id
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val result = service.updateProfile(
            UpdateProfileRequest(
                fullName = userInfo.fullName,
                phone = userInfo.phone,
                timezone = TimeZone.getDefault().id,
                gender = userInfo.gender?.id?.let {
                    if (it.lowercase() == "male" || it.lowercase() == "female") {
                        it.lowercase()
                    } else {
                        org.telegram.divo.entity.mapGenderToEnglish(it) ?: "female"
                    }
                },
                birthday = userInfo.birthday,
                geoCityId = resolvedCityId?.takeIf { it > 0 },
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
        val existingChannels = _currentUserCache.value?.channels ?: emptyList()
        result.toEntity(existingChannels).also { updateCacheAndPersist(it) }
    }

    suspend fun deleteAccount(): DivoResult<Unit> = resultOf {
        service.deleteAccount()
    }

    suspend fun updateProfile(
        request: UpdateProfileRequest
    ): DivoResult<UserInfo> = resultOf {
        val existingChannels = _currentUserCache.value?.channels ?: emptyList()
        service.updateProfile(request).toEntity(existingChannels)
            .also { updateCacheAndPersist(it) }
    }

    private fun updateCacheAndPersist(info: UserInfo) {
        _currentUserCache.value = info
        prefs.edit().apply {
            putInt(KEY_USER_ID, info.id)
            putString(KEY_AVATAR_URL, info.avatarUrl)
            apply()
        }
        
        if (info.role != org.telegram.divo.entity.RoleType.UNKNOWN) {
            DivoAnalytics.setUserProperty("user_role", info.role.value)
        }
        
        scope.launch {
            NotificationCenter.getInstance(accountIndex).postNotificationName(NotificationCenter.divo_userInfoUpdated)
        }
    }

    fun clearCache() {
        DivoApi.accessTokenProvider.setAccessToken(null)
        _currentUserCache.value = null
        _galleryCache.value = emptyMap()
        prefs.edit { clear() }
        scope.launch {
            NotificationCenter.getInstance(accountIndex).postNotificationName(NotificationCenter.divo_userInfoUpdated)
        }
    }

    suspend fun updateAgency(
        agency: Agency
    ): DivoResult<Unit> = resultOf {
        service.updateAgency(
            agency.toDto()
        )

        getCurrentUserInfo(forceRefresh = true)
    }

    suspend fun getAgencyModels(
        agencyId: Int,
        offset: Int = 0,
        limit: Int = 10,
    ): DivoResult<AgencyModels> = resultOf {
        service.getAgencyModels(
            agencyId = agencyId,
            request = AgencyModelsRequest(offset, limit)
        ).toEntities()
    }

    suspend fun searchAgencyModels(
        query: String,
        offset: Int,
        limit: Int,
        currentAgencyId: Int?
    ): DivoResult<PaginatedResult<AgencySearchModel>> = resultOf {
        val res = service.searchAgencyModels(
            org.telegram.divo.dal.dto.user.AgencySearchRequest(
                name = query.takeIf { it.isNotBlank() },
                offset = offset,
                limit = limit
            )
        )
        val entities = res.toEntities(currentAgencyId)
        PaginatedResult(
            items = entities,
            totalCount = res.data?.pagination?.meta?.totalCount ?: entities.size
        )
    }

    suspend fun addAgencyModel(
        userId: Int,
        note: String? = null
    ): DivoResult<Unit> = resultOf {
        service.addAgencyModel(
            userId = userId,
            request = org.telegram.divo.dal.dto.user.AddAgencyModelRequest(note = note)
        )
    }

    suspend fun deleteAgencyModel(
        agencyId: Int,
        modelId: Int
    ): DivoResult<Unit> = resultOf {
        service.deleteAgencyModel(
            agencyId = agencyId,
            modelId = modelId
        )
    }

    suspend fun upsertSocialNetwork(socialNetworkId: Int, nickname: String): DivoResult<Unit> = resultOf {
        service.upsertSocialNetwork(
            UpsertSocialNetworkRequest(socialNetworkId, nickname)
        )
        getCurrentUserInfo(forceRefresh = true)
    }

    fun fetchUserInfoInBackground() {
        scope.launch {
            getCurrentUserInfo(forceRefresh = false)
        }
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

    suspend fun uploadPhotos(files: List<File>): DivoResult<List<UploadedFile>> = resultOf {
        service.uploadFiles(files.map { it.toMultipart() }).data.map { uploadedFile ->
            UploadedFile(
                uuid = uploadedFile.uuid,
                fullUrl = uploadedFile.fullUrl
            )
        }
    }

    suspend fun getUserSocialNetworks(): DivoResult<List<UserSocialNetwork>> = resultOf {
        service.getUserSocialNetworks().toEntities()
    }

    suspend fun getEngagement(userId: Int, offset: Int, limit: Int, search: String = ""): DivoResult<Engagement> = resultOf {
        service.getEngagement(userId = userId, offset = offset, limit = limit, search = search).toEntity()
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

    suspend fun getAppearances(): DivoResult<Appearances> = resultOf {
        service.getAppearances().data.toEntity()
    }

    suspend fun reportProfile(userId: Int, reportKey: String): DivoResult<Unit> = resultOf {
        service.reportProfile(
            ReportProfileRequest(
                reportUserId = userId,
                reportText = reportKey
            )
        )
    }

    suspend fun getFeedReportTypes(): DivoResult<Map<String, String>> = resultOf {
        val data = DivoApi.dictionaryService.getFeedReportTypes().data
        data.associate { it.id to it.title }
    }

    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any?) {
        if (id == NotificationCenter.dialogDeleted) {
            val did = args[0] as? Long ?: return
            if (did < 0) {
                val chatId = -did
                val currentUser = _currentUserCache.value
                if (currentUser != null) {
                    val matchingChannel = currentUser.channels.find { it.telegramChatId == chatId }
                    if (matchingChannel != null) {
                        _currentUserCache.value = currentUser.copy(
                            channels = currentUser.channels.filter { it.telegramChatId != chatId }
                        )
                        scope.launch {
                            deleteChannel(matchingChannel.id)
                        }
                    }
                }
            }
        }
    }
}

