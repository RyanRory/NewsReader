package com.uptick.newsreader.service

import com.uptick.newsreader.data.NewsArticle
import com.uptick.newsreader.repository.NewsRepository

class FetchNewsService(private val repository: NewsRepository) {
    suspend operator fun invoke(
        query: String? = null,
        category: String = "world",
        lang: String = "en",
        page: Int = 1
    ): Result<List<NewsArticle>> = runCatching {
        repository.fetchNews(query, category, lang, page)
    }

    @Throws(Exception::class)
    suspend fun fetch(
        query: String? = null,
        category: String = "world",
        lang: String = "en",
        page: Int = 1
    ): List<NewsArticle> = repository.fetchNews(query, category, lang, page)
}