package org.telegram.divo.dal.dto.face

import com.google.gson.annotations.SerializedName

class FaceDetectResponse(
    @SerializedName("faces") val faces: List<FaceDto>?
)

class FaceDto(
    @SerializedName("area") val area: Double?,
    @SerializedName("bbox") val bbox: BBoxDto?,
    @SerializedName("index") val index: Int
)

class BBoxDto(
    @SerializedName("x1") val x1: Float,
    @SerializedName("x2") val x2: Float,
    @SerializedName("y1") val y1: Float,
    @SerializedName("y2") val y2: Float
)
