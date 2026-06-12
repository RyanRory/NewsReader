package com.uptick.newsreader.repository

import com.uptick.newsreader.data.NewsArticle
import com.uptick.newsreader.data.toNewsArticles
import com.uptick.newsreader.db.DataPersistence
import com.uptick.newsreader.network.GNewsHttpRequests

class NewsRepositoryImpl(
    private val request: GNewsHttpRequests,
    private val db: DataPersistence
) : NewsRepository {

    override suspend fun fetchNews(
        query: String?,
        category: String,
        lang: String,
        page: Int
    ): List<NewsArticle> {
        val articles = request.fetchNews(query, category, lang, page = page)
            .toNewsArticles()

        return articles.map { article ->
            article.copy(isStarred = db.isStarred(article.id))
        }
    }
}