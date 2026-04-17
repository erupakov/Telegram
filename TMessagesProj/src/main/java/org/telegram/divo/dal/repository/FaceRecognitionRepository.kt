package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.Flow
import org.telegram.divo.dal.api.FaceRecognitionService
import org.telegram.divo.dal.db.dao.FaceRecognitionDao
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.dal.dto.face.SearchSimilarRequest
import org.telegram.divo.dal.dto.face.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.SimilarFace

class FaceRecognitionRepository(
    private val dao: FaceRecognitionDao,
    private val service: FaceRecognitionService
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

    suspend fun searchSimilar(photoId: Long, topK: Int = 6): DivoResult<List<SimilarFace>> {
        return resultOf {
            val response = service.searchSimilar(SearchSimilarRequest(photoId, topK))
            response.results?.map { it.toEntity() } ?: emptyList()
        }
    }
}
