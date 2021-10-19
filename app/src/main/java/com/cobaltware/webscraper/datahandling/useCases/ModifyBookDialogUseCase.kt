package com.cobaltware.webscraper.datahandling.useCases

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookDatabase
import com.cobaltware.webscraper.datahandling.repositories.BookListRepository
import com.cobaltware.webscraper.datahandling.repositories.BookRepository
import kotlinx.coroutines.launch

class ModifyBookDialogUseCase(context: Context) : AndroidViewModel(Application()) {

    private val bookRepository: BookRepository
    private val bookListRepository: BookListRepository

    init {
        val database = BookDatabase.getDatabase(context)

        bookRepository = BookRepository(database.bookDao())
        bookListRepository = BookListRepository(database.bookListDao())
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        bookRepository.deleteBook(book)
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        bookRepository.updateBook(book)
    }

    fun addBook(book: Book) = viewModelScope.launch {
        bookRepository.addBook(book)
    }

    fun readAllLists() = bookListRepository.readAllLists()


}