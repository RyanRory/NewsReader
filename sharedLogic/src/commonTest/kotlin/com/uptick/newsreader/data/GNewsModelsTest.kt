package com.uptick.newsreader.data

import kotlinx.serialization.json.Json
import kotlin.test.*

class GNewsModelsTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val rawJson = """
        {
          "totalArticles": 54904,
          "articles": [
            {
              "id": "b961dade95c55b7f949ccd8e0234a356",
              "title": "M5 chip leak reveals Apple has big gains coming in key area",
              "description": "Apple's forthcoming M5 chip has seemingly leaked as part of a new iPad Pro hardware leak. Here's what its performance looks like in testing.",
              "content": "Today, Apple's as-yet-unannounced M5 iPad Pro was seemingly leaked...",
              "url": "https://9to5mac.com/2025/09/30/m5-chip-leak-reveals-apple-has-big-gains-coming-in-key-area/",
              "image": "https://i0.wp.com/9to5mac.com/wp-content/uploads/sites/6/2024/12/M5-Pro-chip-could-separate-CPU-and-GPU-in-server-grade-chips.jpg",
              "publishedAt": "2025-09-30T19:38:25Z",
              "lang": "en",
              "source": {
                "id": "92f73865e835e33ed68c11447777c939",
                "name": "9to5Mac",
                "url": "https://9to5mac.com",
                "country": "us"
              }
            }
          ]
        }
    """.trimIndent()

    // region Parsing

    @Test
    fun `parses totalArticles correctly`() {
        val response = json.decodeFromString<GNewsResponse>(rawJson)
        assertEquals(54904, response.totalArticles)
    }

    @Test
    fun `parses articles list with correct count`() {
        val response = json.decodeFromString<GNewsResponse>(rawJson)
        assertEquals(1, response.articles.size)
    }

    @Test
    fun `parses article fields correctly`() {
        val article = json.decodeFromString<GNewsResponse>(rawJson).articles.first()
        assertEquals("b961dade95c55b7f949ccd8e0234a356", article.id)
        assertEquals("M5 chip leak reveals Apple has big gains coming in key area", article.title)
        assertEquals("https://9to5mac.com/2025/09/30/m5-chip-leak-reveals-apple-has-big-gains-coming-in-key-area/", article.url)
        assertEquals("2025-09-30T19:38:25Z", article.publishedAt)
        assertEquals("en", article.lang)
    }

    @Test
    fun `parses source fields correctly`() {
        val source = json.decodeFromString<GNewsResponse>(rawJson).articles.first().source
        assertEquals("92f73865e835e33ed68c11447777c939", source.id)
        assertEquals("9to5Mac", source.name)
        assertEquals("https://9to5mac.com", source.url)
    }

    @Test
    fun `parses image url correctly`() {
        val article = json.decodeFromString<GNewsResponse>(rawJson).articles.first()
        assertNotNull(article.image)
        assertTrue(article.image!!.startsWith("https://"))
    }

    // endregion

    // region Mapping

    @Test
    fun `maps to NewsArticle correctly`() {
        val article = json.decodeFromString<GNewsResponse>(rawJson).articles.first()
        val newsArticle = article.toNewsArticle()

        assertEquals(article.id,          newsArticle.id)
        assertEquals(article.title,       newsArticle.title)
        assertEquals(article.url,         newsArticle.url)
        assertEquals(article.image,       newsArticle.imageUrl)
        assertEquals(article.lang,        newsArticle.lang)
        assertEquals(article.publishedAt, newsArticle.publishedAt)
        assertEquals(article.source.name, newsArticle.sourceName)
        assertFalse(newsArticle.isStarred)
    }

    @Test
    fun `maps isStarred correctly when url is in starred set`() {
        val article = json.decodeFromString<GNewsResponse>(rawJson).articles.first()
        val starredIds = setOf(article.id)
        val newsArticle = article.toNewsArticle(starredIds)

        assertTrue(newsArticle.isStarred)
    }

    @Test
    fun `maps description to empty string when null`() {
        val jsonWithNullDesc = rawJson.replace(
            "\"description\": \"Apple's forthcoming M5 chip has seemingly leaked as part of a new iPad Pro hardware leak. Here's what its performance looks like in testing.\"",
            "\"description\": null"
        )
        val article = json.decodeFromString<GNewsResponse>(jsonWithNullDesc).articles.first()
        val newsArticle = article.toNewsArticle()

        assertEquals("", newsArticle.description)
    }

    // endregion
}