package com.uptick.newsreader.mock

import com.uptick.newsreader.data.NewsArticle
import com.uptick.newsreader.repository.NewsRepository

class MockNewsRepository: NewsRepository {
    var shouldThrow = false
    var headlines = listOf(
        NewsArticle(
            id = "b961dade95c55b7f949ccd8e0234a356",
            title = "M5 chip leak reveals Apple has big gains coming in key area",
            description = "Apple's forthcoming M5 chip has seemingly leaked as part of a new iPad Pro hardware leak.",
            url = "https://9to5mac.com/2025/09/30/m5-chip-leak-reveals-apple-has-big-gains-coming-in-key-area/",
            imageUrl = "https://i0.wp.com/9to5mac.com/wp-content/uploads/sites/6/2024/12/M5-Pro-chip-could-separate-CPU-and-GPU-in-server-grade-chips.jpg",
            publishedAt = "2025-09-30T19:38:25Z",
            lang = "en",
            sourceName = "9to5Mac",
            isStarred = false
        )
    )

    override suspend fun fetchNews(query: String?, category: String, lang: String, page: Int): List<NewsArticle> {
        if (shouldThrow) throw Exception("Network error")
        return if (query.isNullOrBlank()) {
            headlines
        } else {
            headlines.filter { it.title.contains(query, ignoreCase = true) }
        }
    }
}