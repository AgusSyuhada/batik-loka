package com.bangkit.batikloka.data.remote.api

import com.bangkit.batikloka.data.remote.response.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("news")
    suspend fun getNews(
        @Query("key") key: String = "detail",
        @Query("detail") detailFlag: Boolean = true
    ): Response<NewsResponse>
}