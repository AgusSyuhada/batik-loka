package com.bangkit.batikloka.data.repository

import com.bangkit.batikloka.data.local.dao.NewsDao
import com.bangkit.batikloka.data.local.entity.NewsEntity
import com.bangkit.batikloka.data.remote.api.NewsApiService
import com.bangkit.batikloka.data.remote.response.NewsItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository(
    private val apiService: NewsApiService,
    private val newsDao: NewsDao
) {
    fun getLocalNews(): Flow<List<NewsItem>> {
        return newsDao.getAllNews().map { entityList ->
            entityList.map { entity ->
                NewsItem(
                    body = entity.body,
                    gambar = entity.gambar,
                    judul = entity.judul,
                    link = entity.link,
                    waktu = entity.waktu
                )
            }
        }
    }

    suspend fun getNews(): Result<List<NewsItem>> {
        return try {
            val response = apiService.getNews()

            if (response.isSuccessful) {
                val newsResponse = response.body()
                if (newsResponse?.status == 200) {
                    newsDao.deleteAllNews()

                    val newsEntities = newsResponse.data.map { newsItem ->
                        NewsEntity(
                            body = newsItem.body,
                            gambar = newsItem.gambar,
                            judul = newsItem.judul,
                            link = newsItem.link,
                            waktu = newsItem.waktu
                        )
                    }

                    newsDao.insertNews(newsEntities)

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