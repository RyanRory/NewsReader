package com.uptick.newsreader.service

import com.uptick.newsreader.data.NewsArticle
import com.uptick.newsreader.db.DataPersistence
import com.uptick.newsreader.mock.createTestDatabase
import kotlin.test.*

class DataPersistenceTest {

    private val db = DataPersistence(createTestDatabase())

    private val article = NewsArticle(
        id = "b961dade95c55b7f949ccd8e0234a356",
        title = "M5 chip leak reveals Apple has big gains coming in key area",
        description = "Apple's forthcoming M5 chip has seemingly leaked.",
        url = "https://9to5mac.com/2025/09/30/m5-chip-leak-reveals-apple-has-big-gains-coming-in-key-area/",
        imageUrl = "https://i0.wp.com/9to5mac.com/wp-content/uploads/sites/6/2024/12/M5-Pro-chip-could-separate-CPU-and-GPU-in-server-grade-chips.jpg",
        publishedAt = "2025-09-30T19:38:25Z",
        lang = "en",
        sourceName = "9to5Mac",
        isStarred = false
    )

    private val article2 = article.copy(
        id  = "another-id",
        url = "https://9to5mac.com/another-article/"
    )

    @Test
    fun `starred list is empty initially`() {
        assertEquals(0, db.getStarredArticles().size)
    }

    @Test
    fun `toggleStar adds article when not starred`() {
        db.toggleStar(article)
        assertTrue(db.isStarred(article.id))
    }

    @Test
    fun `toggleStar removes article when already starred`() {
        db.toggleStar(article)
        db.toggleStar(article)
        assertFalse(db.isStarred(article.id))
    }

    @Test
    fun `toggleStar does not affect other articles`() {
        db.toggleStar(article)
        db.toggleStar(article2)
        db.toggleStar(article)

        assertFalse(db.isStarred(article.id))
        assertTrue(db.isStarred(article2.id))
    }

    @Test
    fun `isStarred returns false for unknown id`() {
        assertFalse(db.isStarred("nonexistent-id"))
    }

    @Test
    fun `getStarredArticles returns full article data`() {
        db.toggleStar(article)

        val starred = db.getStarredArticles().first()
        assertEquals(article.id, starred.id)
        assertEquals(article.title, starred.title)
        assertEquals(article.description, starred.description)
        assertEquals(article.url, starred.url)
        assertEquals(article.imageUrl, starred.imageUrl)
        assertEquals(article.publishedAt, starred.publishedAt)
        assertEquals(article.lang, starred.lang)
        assertEquals(article.sourceName, starred.sourceName)
    }

    @Test
    fun `getStarredArticles returns isStarred true`() {
        db.toggleStar(article)
        assertTrue(db.getStarredArticles().first().isStarred)
    }

    @Test
    fun `multiple articles can be starred`() {
        db.toggleStar(article)
        db.toggleStar(article2)
        assertEquals(2, db.getStarredArticles().size)
    }

    @Test
    fun `article with null imageUrl is stored correctly`() {
        val noImage = article.copy(id = "no-image-id", imageUrl = null)
        db.toggleStar(noImage)

        val starred = db.getStarredArticles().first { it.id == noImage.id }
        assertNull(starred.imageUrl)
    }
}