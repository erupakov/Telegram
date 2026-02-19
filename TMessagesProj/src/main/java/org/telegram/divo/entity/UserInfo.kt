package org.telegram.divo.entity

data class UserInfo(
    val id: Long,
    val fullName: String,
    val gender: Gender,
    val birthday: String,
    val city: City,
    val email: String,
    val phone: String,
    val avatarUrl: String?,
    val role: String,
    val statistic: Statistic,
    val isFavorite: Boolean,
    val isFollowed: Boolean
)

data class Gender(
    val id: String,
    val title: String
)

data class City(
    val id: Int,
    val name: String,
    val countryName: String
)

data class Statistic(
    val followersCount: Int,
    val followingCount: Int,
    val viewsCount: Int
)