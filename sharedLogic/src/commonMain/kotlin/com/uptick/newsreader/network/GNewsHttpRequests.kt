package com.uptick.newsreader.network

import com.uptick.newsreader.GNEWS_API_KEY
import com.uptick.newsreader.data.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class GNewsHttpRequests(private val client: HttpClient) {
    private val baseURL   = "https://gnews.io/api/v4"
    private val apiKey = GNEWS_API_KEY

    suspend fun fetchNews(
        query: String? = null,
        category: String = "world",
        lang: String = "en",
        max: Int = 10,
        page: Int = 1
    ): GNewsResponse {
        val endpoint = if (!query.isNullOrBlank()) "search" else "top-headlines"
        return client.get("$baseURL/$endpoint") {
            query?.let { parameter("q", it) }
            parameter("category", category)
            parameter("lang", lang)
            parameter("max", max)
            parameter("page", page)
            parameter("apikey", apiKey)
        }.body<GNewsResponse>()
    }
}