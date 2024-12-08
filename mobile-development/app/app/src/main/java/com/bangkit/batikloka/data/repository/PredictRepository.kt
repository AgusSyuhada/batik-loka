package com.bangkit.batikloka.data.repository

import com.bangkit.batikloka.data.remote.api.PredictApiService
import com.bangkit.batikloka.data.remote.response.PredictResponse
import com.bangkit.batikloka.utils.ScanResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class PredictRepository(
    private val apiService: PredictApiService
) {
    suspend fun predictBatik(
        imageFile: File
    ): ScanResult<PredictResponse> {
        return try {
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val input = MultipartBody.Part.createFormData(
                "input",
                imageFile.name,
                requestFile
            )

            val response = apiService.predictBatik(input)
            ScanResult.Success(response)
        } catch (e: HttpException) {
            val errorMessage = try {
                e.response()?.errorBody()?.string() ?: "Prediction failed"
            } catch (ioException: IOException) {
                "Prediction failed"
            }

            ScanResult.Error(errorMessage)
        } catch (e: Exception) {
            ScanResult.Error(e.localizedMessage ?: "Batik prediction failed")
        }
    }
}