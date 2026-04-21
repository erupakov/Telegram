package org.telegram.divo.dal.network

import org.telegram.divo.common.utils.ThumbnailProcessor
import org.telegram.divo.dal.api.FaceRecognitionService
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.api.DictionaryService
import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.api.SystemService
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.api.WalletService
import org.telegram.divo.dal.api.WorkHistory
import org.telegram.divo.dal.db.AppDatabase
import org.telegram.divo.dal.db.dao.FaceRecognitionDao
import org.telegram.divo.dal.repository.AuthRepository
import org.telegram.divo.dal.repository.EventRepository
import org.telegram.divo.dal.repository.FaceRecognitionRepository
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.dal.repository.UserRepository
import org.telegram.divo.dal.repository.WalletRepository
import org.telegram.divo.dal.repository.WorkHistoryRepository
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import retrofit2.Retrofit

/**
 * Entry point for accessing Divo DAOs.
 */
object DivoApi {

    private val applicationContext
        get() = ApplicationLoader.applicationContext

    private val accessTokenProvider: AccessTokenProvider by lazy {
        SharedPrefsAccessTokenProvider(applicationContext)
    }

    private val retrofit: Retrofit by lazy {
        val client = DivoApiClient.createOkHttpClient(accessTokenProvider)
        DivoApiClient.createRetrofit(client)
    }

    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    private val faceRecognitionDao: FaceRecognitionDao by lazy {
        appDatabase.faceRecognitionDao()
    }

    val authRepository: AuthRepository by lazy {
        val service = retrofit.create(AuthService::class.java)
        AuthRepository(service, accessTokenProvider)
    }

    val workHistory: WorkHistoryRepository by lazy {
        val service = retrofit.create(WorkHistory::class.java)
        WorkHistoryRepository(service)
    }

    private val userRepositoryInstances: Array<UserRepository> by lazy {
        Array(UserConfig.MAX_ACCOUNT_COUNT) { i ->
            val service = retrofit.create(UserService::class.java)
            val prefs = applicationContext.getSharedPreferences("divo_user_cache_$i", android.content.Context.MODE_PRIVATE)
            UserRepository(service, prefs, i)
        }
    }

    val userRepository: UserRepository
        get() = userRepositoryInstances[UserConfig.selectedAccount]

    val publicationRepository: PublicationRepository by lazy {
        val service = retrofit.create(PublicationService::class.java)
        val thumbnailProcessor = ThumbnailProcessor()
        PublicationRepository(service, thumbnailProcessor)
    }

    val eventRepository: EventRepository by lazy {
        val service = retrofit.create(EventService::class.java)
        EventRepository(service)
    }

    val faceRecognitionRepository by lazy {
        val service = retrofit.create(FaceRecognitionService::class.java)
        FaceRecognitionRepository(faceRecognitionDao, service)
    }

    val walletRepository: WalletRepository by lazy {
        val service = retrofit.create(WalletService::class.java)
        WalletRepository(service)
    }

    val systemService: SystemService by lazy {
        retrofit.create(SystemService::class.java)
    }

    val dictionaryService: DictionaryService by lazy {
        retrofit.create(DictionaryService::class.java)
    }
}

