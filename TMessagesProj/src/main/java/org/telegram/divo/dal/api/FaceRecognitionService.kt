package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.face.SearchSimilarRequest
import org.telegram.divo.dal.dto.face.SimilarFaceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FaceRecognitionService {

    @POST("fr/searchSimilar")
    suspend fun searchSimilar(
        @Body request: SearchSimilarRequest
    ): SimilarFaceResponse
}
