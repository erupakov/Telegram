package org.telegram.divo.entity

data class UserSocialNetwork(
    val id: Int,
    val name: String,
    val link: String,
    val provider: String,
)