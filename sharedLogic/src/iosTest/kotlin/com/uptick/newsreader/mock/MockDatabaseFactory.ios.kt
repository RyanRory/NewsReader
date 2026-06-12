package com.uptick.newsreader.mock

import app.cash.sqldelight.driver.native.inMemoryDriver
import com.uptick.newsreader.db.NewsDatabase

actual fun createTestDatabase(): NewsDatabase {
    val driver = inMemoryDriver(NewsDatabase.Schema)
    return NewsDatabase(driver)
}