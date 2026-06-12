package com.uptick.newsreader.service

import com.uptick.newsreader.mock.MockNewsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FetchNewsServiceTest {

    private val repo    = MockNewsRepository()
    private val service = FetchNewsService(repo)

    // region invoke

    @Test
    fun `invoke returns headlines when query is null`() = runTest {
        val result = service.invoke(category = "world", lang = "en")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("M5 chip leak reveals Apple has big gains coming in key area", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `invoke returns matching articles for search query`() = runTest {
        val result = service.invoke(query = "M5", category = "world", lang = "en")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `invoke returns empty list for non matching query`() = runTest {
        val result = service.invoke(query = "zzznomatch", category = "world", lang = "en")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        repo.shouldThrow = true
        val result = service.invoke(category = "world", lang = "en")
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // endregion

    // region fetch

    @Test
    fun `fetch returns headlines when query is null`() = runTest {
        val articles = service.fetch(category = "world", lang = "en")
        assertEquals(1, articles.size)
        assertEquals("9to5Mac", articles.first().sourceName)
    }

    @Test
    fun `fetch returns matching articles for search query`() = runTest {
        val articles = service.fetch(query = "Apple", category = "world", lang = "en")
        assertEquals(1, articles.size)
        assertEquals("https://9to5mac.com/2025/09/30/m5-chip-leak-reveals-apple-has-big-gains-coming-in-key-area/", articles.first().url)
    }

    @Test
    fun `fetch returns empty for non matching query`() = runTest {
        val articles = service.fetch(query = "zzznomatch", category = "world", lang = "en")
        assertEquals(0, articles.size)
    }

    @Test
    fun `fetch throws when repository throws`() = runTest {
        repo.shouldThrow = true
        assertFailsWith<Exception> {
            service.fetch(category = "world", lang = "en")
        }
    }

    // endregion
}