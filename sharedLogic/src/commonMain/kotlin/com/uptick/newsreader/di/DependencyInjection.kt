package com.uptick.newsreader.di

import com.uptick.newsreader.db.DataPersistence
import com.uptick.newsreader.db.NewsDatabase
import com.uptick.newsreader.network.*
import com.uptick.newsreader.db.provideDatabaseDriver
import com.uptick.newsreader.repository.*
import com.uptick.newsreader.service.*
import org.koin.core.context.startKoin
import org.koin.dsl.module

val dataModule = module {
    single { NewsDatabase(provideDatabaseDriver()) }
    single { DataPersistence(get()) }
    single<NewsRepository> { NewsRepositoryImpl(get(), get()) }
}

val networkModule = module {
    single { createHttpClient() }
    single { GNewsHttpRequests(get()) }
}

val serviceModule = module {
    factory { FetchNewsService(get()) }
    factory { ToggleStarService(get()) }
}

val appModule = listOf(networkModule, dataModule, serviceModule)

fun initKoin() {
    startKoin {
        printLogger()
        modules(appModule)
    }
}