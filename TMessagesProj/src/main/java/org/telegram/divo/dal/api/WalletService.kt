package org.telegram.divo.dal.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Wallet and paid services endpoints.
 */
interface WalletService {

    @POST("wallet/list")
    suspend fun listWallets(@Body body: Map<String, Any?>): Response<ResponseBody>

    @GET("wallet/{walletId}")
    suspend fun getWallet(@Path("walletId") walletId: Long): Response<ResponseBody>

    @POST("wallet/{walletId}/operations")
    suspend fun getOperations(
        @Path("walletId") walletId: Long,
        @Body body: Map<String, Any?>
    ): Response<ResponseBody>
}


