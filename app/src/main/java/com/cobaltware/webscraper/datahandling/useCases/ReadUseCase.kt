package com.cobaltware.webscraper.datahandling.useCases

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookDatabase
import com.cobaltware.webscraper.datahandling.repositories.BookRepository
import com.cobaltware.webscraper.datahandling.repositories.ConfigRepository
import kotlinx.coroutines.launch

class ReadUseCase(context: Context) : AndroidViewModel(Application()) {
    private val configRepository: ConfigRepository
    private val bookRepository: BookRepository

    init {
        val database = BookDatabase.getDatabase(context)

        configRepository = ConfigRepository(database.configDao())
        bookRepository = BookRepository(database.bookDao())
    }

    fun readItemFromConfigs(domain: String) = configRepository.readItemFromConfigs(domain)
    fun updateBook(book: Book) = viewModelScope.launch { bookRepository.updateBook(book) }
}