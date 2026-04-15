package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.Flow
import org.telegram.divo.dal.db.dao.FaceRecognitionDao
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity

class FaceRecognitionRepository(
    private val dao: FaceRecognitionDao
) {
    fun getAll(): Flow<List<FaceRecognitionEntity>> {
        return dao.getAll()
    }

    fun getRecent(limit: Int = 10, userId: Int): Flow<List<FaceRecognitionEntity>> {
        return dao.getRecent(limit, userId)
    }

    fun observeByUser(userId: Int): Flow<List<FaceRecognitionEntity>> {
        return dao.observeByUser(userId)
    }

    suspend fun getByImageUri(imageUri: String, userId: Int): FaceRecognitionEntity? {
        return dao.getByImageUri(imageUri, userId)
    }

    suspend fun save(entity: FaceRecognitionEntity) {
        dao.insert(entity)
    }

    suspend fun delete(entity: FaceRecognitionEntity) {
        dao.delete(entity)
    }

    suspend fun clearHistory(userId: Int) {
        dao.deleteAll(userId)
    }
}
