package com.uptick.newsreader.mock

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.uptick.newsreader.db.NewsDatabase

actual fun createTestDatabase(): NewsDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    NewsDatabase.Schema.create(driver)
    return NewsDatabase(driver)
}