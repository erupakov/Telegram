package org.telegram.divo.dal.repository

import org.telegram.divo.dal.api.DictionaryService
import org.telegram.divo.dal.dto.payment.PaymentsResponse
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf

class PaymentRepository(
    private val service: DictionaryService
) {
    suspend fun getPayments(): DivoResult<PaymentsResponse> = resultOf {
        service.getPayments()
    }
}
