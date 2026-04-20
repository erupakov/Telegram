package org.telegram.divo.dal.dto.face

import com.google.gson.annotations.SerializedName

data class SearchSimilarRequest(
    @SerializedName("photo_id") val photoId: Long,
    @SerializedName("top_k") val topK: Int
)
