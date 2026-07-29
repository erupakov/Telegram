package org.telegram.divo.entity

data class SavedProfile(
    val id: Int,
    val fullName: String,
    val role: String,
    val roleLabel: String,
    val avatarUrl: String
)
