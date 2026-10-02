package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

class LoginResponse(
    @SerializedName("data") val data: LoginData?,
) {
    class LoginData(
        @SerializedName("accessToken") val accessToken: String?
    )
}


