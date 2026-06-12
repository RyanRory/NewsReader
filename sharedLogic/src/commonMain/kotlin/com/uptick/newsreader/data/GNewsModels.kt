package com.uptick.newsreader.data

import kotlinx.serialization.Serializable

// region RemoteDataModel

@Serializable
data class GNewsResponse(
    val totalArticles: Int,
    val articles: List<GNewsArticle>
)

@Serializable
data class GNewsArticle(
    val id: String,
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val image: String?,
    val publishedAt: String,
    val lang: String,
    val source: GNewsSource
)

@Serializable
data class GNewsSource(
    val id: String,
    val name: String,
    val url: String
)

// endregion

// region Mapping

fun GNewsArticle.toNewsArticle(starredIds: Set<String> = emptySet()) = NewsArticle(
    id = id,
    title = title,
    description = description ?: "",
    url = url,
    imageUrl = image,
    publishedAt = publishedAt,
    lang = lang,
    sourceName = source.name,
    isStarred = id in starredIds
)

fun GNewsResponse.toNewsArticles(starredIds: Set<String> = emptySet()) =
    articles.map { it.toNewsArticle(starredIds) }

// endregion