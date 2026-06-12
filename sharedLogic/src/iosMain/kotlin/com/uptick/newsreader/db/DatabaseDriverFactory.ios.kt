package com.uptick.newsreader.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun provideDatabaseDriver(): SqlDriver =
    NativeSqliteDriver(NewsDatabase.Schema, "news.db")