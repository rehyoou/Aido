package com.rr.edito.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface GeminiApi {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content
)

