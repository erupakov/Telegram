package org.telegram.divo.dal.repository

import okhttp3.ResponseBody
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.WalletService

class WalletRepository(
    private val service: WalletService
) {

    suspend fun listWallets(filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return resultOf { service.listWallets(filters) }
    }

    suspend fun getWallet(walletId: Long): DivoResult<ResponseBody> {
        return resultOf { service.getWallet(walletId) }
    }

    suspend fun getOperations(walletId: Long, filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return resultOf { service.getOperations(walletId, filters) }
    }
}

