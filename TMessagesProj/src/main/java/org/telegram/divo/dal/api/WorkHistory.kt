package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.common.EmptyResponse
import org.telegram.divo.dal.dto.work_history.CreateWorkExperienceRequest
import org.telegram.divo.dal.dto.work_history.WorkExperienceResponse
import org.telegram.divo.dal.dto.work_history.WorkHistoryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkHistory {

    @POST("model-work-history")
    suspend fun createWorkExperience(
        @Body request: CreateWorkExperienceRequest
    ): WorkExperienceResponse

    @GET("model-work-history")
    suspend fun getWorkHistory(): WorkHistoryResponse

    @DELETE("model-work-history/{id}")
    suspend fun deleteWorkExperience(
        @Path("id") id: Int
    ): EmptyResponse

    @POST("model-work-history/{id}")
    suspend fun updateWorkExperience(
        @Path("id") id: Int,
        @Body request: CreateWorkExperienceRequest
    ): WorkExperienceResponse
}