package com.cobaltware.webscraper

import android.app.Application
import com.cobaltware.webscraper.datahandling.BookViewModel

class ReaderApplication() : Application() {
    companion object {
        lateinit var DB: BookViewModel
    }

    override fun onCreate() {
        super.onCreate()
        DB = BookViewModel(this)
    }
}