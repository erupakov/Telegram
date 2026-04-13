package org.telegram.divo.entity

data class FeedlineSearchResult(
    val items: List<FeedlineItem>,
    val pagination: Pagination? = null
)

data class FeedlineItem(
    val id: Int,
    val feedId: Int?,
    val title: String,
    val description: String?,
    val type: String?,
    val likesCount: Int,
    val isLikedByUser: Boolean,
    val isFavoriteByUser: Boolean,
    val user: FeedlineUser?,
    val files: List<String>,
    val searchImageUrl: String?
)

data class FeedlineUser(
    val id: Int,
    val fullName: String,
    val role: String,
    val roleLabel: String,
    val city: City?,
    val age: Int?,
    val height: Float?,
    val weight: Float?,
    val emojiCounts: EmojiCounts?,
    val totalEmojisCount: Int,
)

data class RangeParam(
    val from: Int?,
    val to: Int?
)

data class EmojiCounts(
    val thumbsUp: Int,
    val thumbsDown: Int,
    val heart: Int,
    val fire: Int,
)

data class ModelParameters(
    val gender: List<String>? = null,
    val geoCityId: Int? = null,
    val age: RangeParam? = null,
    val weight: RangeParam? = null,
    val height: RangeParam? = null,
    val breastSize: RangeParam? = null,
    val waist: RangeParam? = null,
    val shoesSize: RangeParam? = null,
    val hips: RangeParam? = null,
    val eyeColor: List<Int>? = null,
    val skinColor: List<Int>? = null,
    val hairColor: List<Int>? = null,
    val hairLength: List<Int>? = null
)