package com.uptick.newsreader.service

import com.uptick.newsreader.db.DataPersistence
import com.uptick.newsreader.data.NewsArticle

class ToggleStarService(private val db: DataPersistence) {
    @Throws(Exception::class)
    operator fun invoke(article: NewsArticle) {
        db.toggleStar(article)
    }

    @Throws(Exception::class)
    fun toggleStar(article: NewsArticle) {
        db.toggleStar(article)
    }

    fun isStarred(article: NewsArticle): Boolean =
        db.isStarred(article.url)

    @Throws(Exception::class)
    fun getStarredArticles(): List<NewsArticle> =
        db.getStarredArticles()
}