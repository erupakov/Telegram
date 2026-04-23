package org.telegram.divo.dal.dto.face

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.SimilarFace

class SimilarFaceResponse(
    @SerializedName("photo_id") val photoId: Int?,
    @SerializedName("results") val results: List<SimilarFaceDto>?
)

class SimilarFaceDto(
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("country_name") val countryName: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("index") val index: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("role") val role: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("user_id") val userId: Int?
)

fun SimilarFaceDto.toEntity() = SimilarFace(
    birthday = birthday.orEmpty(),
    countryCode = countryCode.orEmpty(),
    countryName = countryName.orEmpty(),
    fullName = fullName.orEmpty(),
    image = image.orEmpty(),
    index = index ?: 0,
    rank = rank ?: 0,
    role = role.orEmpty(),
    score = score ?: 0.0,
    userId = userId ?: 0
)
