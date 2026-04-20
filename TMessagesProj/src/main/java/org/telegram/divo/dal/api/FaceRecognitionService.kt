package org.telegram.divo.dal.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.telegram.divo.dal.dto.face.FaceDetectResponse
import org.telegram.divo.dal.dto.face.FaceSearchResponse
import org.telegram.divo.dal.dto.face.SearchSimilarRequest
import org.telegram.divo.dal.dto.face.SimilarFaceResponse
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FaceRecognitionService {

    @POST("fr/searchSimilar")
    suspend fun searchSimilar(
        @Body request: SearchSimilarRequest
    ): SimilarFaceResponse

    @Multipart
    @POST("fr/detect")
    suspend fun detectFaces(
        @Part file: MultipartBody.Part
    ): FaceDetectResponse

    @Multipart
    @POST("fr/search")
    suspend fun searchFaces(
        @Part file: MultipartBody.Part,
        @Part("k_ratio") kRatio: RequestBody,
        @Part("top_k") topK: RequestBody,
        @Part("face_index") faceIndex: RequestBody
    ): FaceSearchResponse
}
