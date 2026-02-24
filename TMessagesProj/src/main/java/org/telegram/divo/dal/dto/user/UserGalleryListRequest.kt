package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class UserGalleryListRequest(
    @SerializedName("offset") val offset: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("userId") val userId: Int
)