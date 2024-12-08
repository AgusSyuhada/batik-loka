package com.bangkit.batikloka.data.repository

import com.bangkit.batikloka.data.remote.api.NewsApiService
import com.bangkit.batikloka.data.remote.response.NewsItem

class NewsRepository(
    private val apiService: NewsApiService
) {
    suspend fun getNews(): Result<List<NewsItem>> {
        return try {
            val response = apiService.getNews()

            if (response.isSuccessful) {
                val newsResponse = response.body()
                if (newsResponse?.status == 200) {
                    Result.success(newsResponse.data)
                } else {
                    Result.failure(Exception("Failed to fetch news"))
                }
            } else {
                val errorMessage = response.errorBody()?.string()
                    ?: "Failed to fetch news"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}