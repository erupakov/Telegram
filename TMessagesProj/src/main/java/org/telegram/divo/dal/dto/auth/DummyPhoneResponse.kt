package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class DummyPhoneResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: DummyPhoneData?
) {
    data class DummyPhoneData(
        @SerializedName("phone") val phone: String
    )
}
