package org.telegram.divo.dal

import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.api.DictionaryService
import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.api.SystemService
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.api.WalletService
import org.telegram.divo.dal.dao.AuthDao
import org.telegram.divo.dal.dao.EventDao
import org.telegram.divo.dal.dao.PublicationDao
import org.telegram.divo.dal.dao.UserDao
import org.telegram.divo.dal.dao.WalletDao
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

    val authDao: AuthDao by lazy {
        val service = retrofit.create(AuthService::class.java)
        AuthDao(service, accessTokenProvider)
    }

    val userDao: UserDao by lazy {
        val service = retrofit.create(UserService::class.java)
        UserDao(service)
    }

    val publicationDao: PublicationDao by lazy {
        val service = retrofit.create(PublicationService::class.java)
        PublicationDao(service)
    }

    val eventDao: EventDao by lazy {
        val service = retrofit.create(EventService::class.java)
        EventDao(service)
    }

    val walletDao: WalletDao by lazy {
        val service = retrofit.create(WalletService::class.java)
        WalletDao(service)
    }

    val systemService: SystemService by lazy {
        retrofit.create(SystemService::class.java)
    }

    val dictionaryService: DictionaryService by lazy {
        retrofit.create(DictionaryService::class.java)
    }
}

