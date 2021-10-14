package com.cobaltware.webscraper.datahandling.useCases

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookDatabase
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.repositories.BookListRepository
import com.cobaltware.webscraper.datahandling.repositories.BookRepository
import kotlinx.coroutines.launch

class MainUseCase(context: Context) : AndroidViewModel(Application()) {
    private val bookListRepository: BookListRepository
    private val bookRepository: BookRepository

    init {
        val database = BookDatabase.getDatabase(context)
        bookListRepository = BookListRepository(database.bookListDao())
        bookRepository = BookRepository(database.bookDao())
    }

    fun deleteList(list: BookList) = viewModelScope.launch {
        bookListRepository.deleteList(list)
        bookRepository.deleteFromBookList(list.name)
    }

    fun updateList(newName: String, oldName: String) =
        viewModelScope.launch { bookListRepository.updateList(newName, oldName) }

    fun addList(bookList: BookList) =
        viewModelScope.launch { bookListRepository.addList(bookList) }

    fun readAllLists() = bookListRepository.readAllLists()

    fun readAllFromBookList(list: String): LiveData<List<Book>> =
        bookListRepository.readAllFromBookList(list)
}