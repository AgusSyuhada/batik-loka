package com.bangkit.batikloka.data.remote.api

import com.bangkit.batikloka.data.remote.response.PredictResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PredictApiService {
    @Multipart
    @POST("predict")
    suspend fun predictBatik(
        @Part input: MultipartBody.Part
    ): PredictResponse
}