package org.telegram.divo.dal.dto.payment

import com.google.gson.annotations.SerializedName

class PaymentsResponse(
    @SerializedName("data") val data: PaymentsData
)

class PaymentsData(
    @SerializedName("paymentType") val paymentType: List<PaymentTypeDto>,
    @SerializedName("paymentFrequency") val paymentFrequency: List<PaymentFrequencyDto>
)

class PaymentTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class PaymentFrequencyDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)
