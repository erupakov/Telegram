package org.telegram.divo.dal.network

import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.api.DictionaryService
import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.api.SystemService
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.api.WalletService
import org.telegram.divo.dal.repository.AuthRepository
import org.telegram.divo.dal.repository.EventRepository
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.dal.repository.UserRepository
import org.telegram.divo.dal.repository.WalletRepository
import org.telegram.messenger.ApplicationLoader
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

    val authRepository: AuthRepository by lazy {
        val service = retrofit.create(AuthService::class.java)
        AuthRepository(service, accessTokenProvider)
    }

    val userRepository: UserRepository by lazy {
        val service = retrofit.create(UserService::class.java)
        UserRepository(service)
    }

    val publicationRepository: PublicationRepository by lazy {
        val service = retrofit.create(PublicationService::class.java)
        PublicationRepository(service)
    }

    val eventRepository: EventRepository by lazy {
        val service = retrofit.create(EventService::class.java)
        EventRepository(service)
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

