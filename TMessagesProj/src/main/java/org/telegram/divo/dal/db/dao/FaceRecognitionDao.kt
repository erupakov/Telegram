package org.telegram.divo.dal.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity

@Dao
interface FaceRecognitionDao {
    @Query("SELECT * FROM face_recognition ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FaceRecognitionEntity>>

    @Query("SELECT * FROM face_recognition WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int, userId: Int): Flow<List<FaceRecognitionEntity>>

    @Query("SELECT * FROM face_recognition WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: Int): Flow<List<FaceRecognitionEntity>>

    @Query("SELECT * FROM face_recognition WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getHistoryPage(userId: Int, limit: Int, offset: Int): List<FaceRecognitionEntity>

    @Query("SELECT COUNT(*) FROM face_recognition WHERE userId = :userId")
    suspend fun getHistoryCount(userId: Int): Int

    @Query("SELECT * FROM face_recognition WHERE id = :id")
    suspend fun getById(id: String): FaceRecognitionEntity?

    @Query("SELECT * FROM face_recognition WHERE imageUri = :imageUri AND userId = :userId LIMIT 1")
    suspend fun getByImageUri(imageUri: String, userId: Int): FaceRecognitionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FaceRecognitionEntity)

    @Delete
    suspend fun delete(entity: FaceRecognitionEntity)

    @Query("DELETE FROM face_recognition WHERE userId = :userId")
    suspend fun deleteAll(userId: Int)
}
