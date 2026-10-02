package org.telegram.divo.dal.dto.face

import com.google.gson.annotations.SerializedName

class FaceSearchResponse(
    @SerializedName("bbox") val bbox: BBoxDto?,
    @SerializedName("face_info") val faceInfo: FaceInfoDto?,
    @SerializedName("results") val results: List<SimilarFaceDto>?
)

class FaceInfoDto(
    @SerializedName("age") val age: String?,
    @SerializedName("emotion") val emotion: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("race") val race: String?
)
