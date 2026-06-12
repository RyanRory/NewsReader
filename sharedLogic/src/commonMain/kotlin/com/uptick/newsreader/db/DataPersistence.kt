package com.uptick.newsreader.db

import com.uptick.newsreader.data.NewsArticle

class DataPersistence(database: NewsDatabase) {

    private val queries  = database.starredArticleQueries

    @Throws(Exception::class)
    fun getStarredArticles(): List<NewsArticle> =
        queries.selectAll().executeAsList().map { it.toNewsArticle() }

    @Throws(Exception::class)
    fun toggleStar(article: NewsArticle) {
        val exists = queries.countById(article.id).executeAsOne() > 0
        if (exists) {
            queries.deleteById(article.id)
        } else {
            queries.insertOrReplace(
                id = article.id,
                title = article.title,
                description = article.description,
                url = article.url,
                imageUrl = article.imageUrl,
                publishedAt = article.publishedAt,
                lang = article.lang,
                sourceName = article.sourceName
            )
        }
    }

    @Throws(Exception::class)
    fun isStarred(id: String): Boolean =
        queries.countById(id).executeAsOne() > 0
}

private fun StarredArticle.toNewsArticle() = NewsArticle(
    id = id,
    title = title,
    description = description,
    url = url,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    lang = lang,
    sourceName = sourceName,
    isStarred = true
)