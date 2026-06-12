package com.uptick.newsreader.di

import com.uptick.newsreader.service.FetchNewsService
import com.uptick.newsreader.service.ToggleStarService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class KoinHelper : KoinComponent {
    @Throws(Exception::class)
    fun getFetchNewsService(): FetchNewsService = get()

    @Throws(Exception::class)
    fun getToggleStarService(): ToggleStarService = get()
}