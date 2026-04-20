package org.telegram.divo.dal.dto.face

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.SimilarFace

class SimilarFaceResponse(
    @SerializedName("photo_id") val photoId: Int?,
    @SerializedName("results") val results: List<SimilarFaceDto>?
)

class SimilarFaceDto(
    @SerializedName("city") val city: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("index") val index: Int?,
    @SerializedName("photo_id") val photoId: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("user_id") val userId: Int?
)

fun SimilarFaceDto.toEntity() = SimilarFace(
    city = city.orEmpty(),
    fullName = fullName.orEmpty(),
    gender = gender.orEmpty(),
    image = image.orEmpty(),
    index = index ?: 0,
    photoId = photoId ?: 0,
    rank = rank ?: 0,
    rating = rating.orEmpty(),
    score = score ?: 0.0,
    userId = userId ?: 0
)
