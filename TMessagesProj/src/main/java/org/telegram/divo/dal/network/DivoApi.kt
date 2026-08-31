package org.telegram.divo.dal.network

import org.telegram.divo.common.utils.ThumbnailProcessor
import org.telegram.divo.dal.api.FaceRecognitionService
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.api.DictionaryService
import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.api.GeoService
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.api.SystemService
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.api.WorkHistory
import org.telegram.divo.dal.db.AppDatabase
import org.telegram.divo.dal.db.dao.FaceRecognitionDao
import org.telegram.divo.dal.repository.AuthRepository
import org.telegram.divo.dal.repository.LocationRepository
import org.telegram.divo.dal.repository.EventRepository
import org.telegram.divo.dal.repository.FaceRecognitionRepository
import org.telegram.divo.dal.repository.PaymentRepository
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.dal.repository.UserRepository
import org.telegram.divo.dal.repository.WorkHistoryRepository
import org.telegram.divo.dal.utils.AccessTokenProvider
import org.telegram.divo.dal.utils.SharedPrefsAccessTokenProvider
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import retrofit2.Retrofit

/**
 * Entry point for accessing Divo DAOs.
 */
object DivoApi {

    private val applicationContext
        get() = ApplicationLoader.applicationContext

    val accessTokenProvider: AccessTokenProvider by lazy {
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

    val systemService: SystemService by lazy {
        retrofit.create(SystemService::class.java)
    }

    val paymentRepository: PaymentRepository by lazy {
        val service = retrofit.create(DictionaryService::class.java)
        PaymentRepository(service)
    }

    val dictionaryService: DictionaryService by lazy {
        retrofit.create(DictionaryService::class.java)
    }

    val geoService: GeoService by lazy {
        retrofit.create(GeoService::class.java)
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepository()
    }
}

