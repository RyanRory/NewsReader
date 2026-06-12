package com.uptick.newsreader.data

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String,
    val lang: String,
    val sourceName: String,
    val isStarred: Boolean = false  // LocalDataSource
)