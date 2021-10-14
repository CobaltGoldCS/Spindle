package com.cobaltware.webscraper

import android.app.Application

class ReaderApplication : Application() {
    companion object {
        var currentTable = "Books"
    }
}