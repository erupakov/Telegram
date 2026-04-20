package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.telegram.divo.dal.api.FaceRecognitionService
import org.telegram.divo.dal.db.dao.FaceRecognitionDao
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.dal.dto.face.FaceDetectResponse
import org.telegram.divo.dal.dto.face.FaceSearchResponse
import org.telegram.divo.dal.dto.face.SearchSimilarRequest
import org.telegram.divo.dal.dto.face.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.SimilarFace
import java.io.File

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

    suspend fun detectFaces(file: File): DivoResult<FaceDetectResponse> = resultOf {
        service.detectFaces(file.toMultipart("file"))
    }

    suspend fun search(
        file: File,
        kRatio: Double = 0.5,
        topK: Int = 100,
        faceIndex: Int = 0
    ): DivoResult<FaceSearchResponse> = resultOf {
        service.searchFaces(
            file.toMultipart("file"),
            kRatio.toString().toRequestBody("text/plain".toMediaType()),
            topK.toString().toRequestBody("text/plain".toMediaType()),
            faceIndex.toString().toRequestBody("text/plain".toMediaType())
        )
    }

    private fun File.toMultipart(name: String): MultipartBody.Part {
        val requestBody = asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData(
            name = name,
            filename = this.name,
            body = requestBody
        )
    }
}
