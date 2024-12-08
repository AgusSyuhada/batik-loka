package com.bangkit.batikloka.data.remote.api

import android.content.Context
import android.util.Log
import com.bangkit.batikloka.utils.PreferencesManager
import com.yalantis.ucrop.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL_AUTH = "http://34.101.136.170:5000/"
    private const val BASE_URL_SERVICE = "http://34.101.136.170:4000/"

    fun getAuthApiService(
        context: Context,
        preferencesManager: PreferencesManager
    ): AuthApiService {
        return createRetrofit(BASE_URL_AUTH, preferencesManager).create(AuthApiService::class.java)
    }

    fun getServiceApiService(
        context: Context,
        preferencesManager: PreferencesManager
    ): NewsApiService {
        return createRetrofit(
            BASE_URL_SERVICE,
            preferencesManager
        ).create(NewsApiService::class.java)
    }

    fun getPredictApiService(
        context: Context,
        preferencesManager: PreferencesManager
    ): PredictApiService {
        return createRetrofit(
            BASE_URL_SERVICE,
            preferencesManager
        ).create(PredictApiService::class.java)
    }

    private fun createRetrofit(baseUrl: String, preferencesManager: PreferencesManager): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val debugInterceptor = Interceptor { chain ->
            val request = chain.request()

            Log.d("Network", "Sending request: ${request.url}")
            Log.d("Network", "Request Method: ${request.method}")

            request.headers.forEach { (name, value) ->
                Log.d("Network", "Header: $name = $value")
            }

            val response = chain.proceed(request)

            Log.d("Network", "Response Code: ${response.code}")

            response
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")

            preferencesManager.getAccessToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}