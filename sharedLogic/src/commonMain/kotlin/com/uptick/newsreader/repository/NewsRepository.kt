package com.uptick.newsreader.repository

import com.uptick.newsreader.data.NewsArticle

interface NewsRepository {
    suspend fun fetchNews(query: String?, category: String, lang: String, page: Int): List<NewsArticle>
}