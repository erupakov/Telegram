package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.payment.PaymentsResponse
import retrofit2.http.GET

/**
 * Dictionary endpoints for static reference data.
 */
interface DictionaryService {

    @GET("dictionary/payments")
    suspend fun getPayments(): PaymentsResponse
}

