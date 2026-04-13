package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

class FeedlineSearchRequest(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("query")
    val query: String,
    @SerializedName("role")
    val role: List<String>? = null,
    @SerializedName("isSkills")
    val isSkills: Boolean? = null,
    @SerializedName("isEvents")
    val isEvents: Boolean? = null,
    @SerializedName("isProfiles")
    val isProfiles: Boolean? = null,
    @SerializedName("isPosts")
    val isPosts: Boolean? = null,
    @SerializedName("withoutNfts")
    val withoutNfts: Boolean? = null,
    @SerializedName("subscribedOnly")
    val subscribedOnly: Boolean? = null,
    @SerializedName("modelsOnly")
    val modelsOnly: Boolean? = null,
    @SerializedName("modelParameters")
    val modelParameters: ModelParametersDto? = null
)

class ModelParametersDto(
    @SerializedName("gender")
    val gender: List<String>? = null,
    @SerializedName("geoCityId")
    val geoCityId: Int? = null,
    @SerializedName("age")
    val age: RangeParamDto? = null,
    @SerializedName("weight")
    val weight: RangeParamDto? = null,
    @SerializedName("height")
    val height: RangeParamDto? = null,
    @SerializedName("breastSize")
    val breastSize: RangeParamDto? = null,
    @SerializedName("waist")
    val waist: RangeParamDto? = null,
    @SerializedName("shoesSize")
    val shoesSize: RangeParamDto? = null,
    @SerializedName("hips")
    val hips: RangeParamDto? = null,
    @SerializedName("eyeColor")
    val eyeColor: List<Int>? = null,
    @SerializedName("skinColor")
    val skinColor: List<Int>? = null,
    @SerializedName("hairColor")
    val hairColor: List<Int>? = null,
    @SerializedName("hairLength")
    val hairLength: List<Int>? = null
)

class RangeParamDto(
    @SerializedName("from")
    val from: Int? = null,
    @SerializedName("to")
    val to: Int? = null
)