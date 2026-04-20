package org.telegram.divo.dal.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "face_recognition")
data class FaceRecognitionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val imageUri: String,
    val resultsCount: Int = 0,
    val userId: Int,
    val filtersJson: String = "",
    val resultsJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
