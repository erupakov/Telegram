package org.telegram.divo.dal.dao

import okhttp3.ResponseBody
import org.telegram.divo.dal.DivoResult
import org.telegram.divo.dal.safeCall
import org.telegram.divo.dal.api.WalletService

class WalletDao(
    private val service: WalletService
) {

    suspend fun listWallets(filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return safeCall { service.listWallets(filters) }
    }

    suspend fun getWallet(walletId: Long): DivoResult<ResponseBody> {
        return safeCall { service.getWallet(walletId) }
    }

    suspend fun getOperations(walletId: Long, filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return safeCall { service.getOperations(walletId, filters) }
    }
}

