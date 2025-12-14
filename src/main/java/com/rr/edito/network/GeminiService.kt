package com.rr.edito.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    fun create(apiKey: String, model: String): GeminiApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    fun getApiUrl(model: String, apiKey: String): String {
        return "${BASE_URL}${model}:generateContent?key=$apiKey"
    }
}

