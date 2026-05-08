package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

class FavoriteRequest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("entity")
    val entity: String
)