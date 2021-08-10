package com.cobaltware.webscraper.datahandling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BookViewModel(application: Application) :
    AndroidViewModel(application) {
    var currentTable: String

    val readAllConfigs: LiveData<List<Config>>
    val readAllBooks: LiveData<List<Book>>
    var readAllLists: LiveData<List<BookList>>
    private val repository: BookRepository


    init {
        val database = BookDatabase.getDatabase(application)
        val bookDao = database.bookDao()
        val configDao = database.configDao()
        currentTable = "Books"

        repository = BookRepository(bookDao, configDao)
        readAllConfigs = repository.readAllConfigs
        readAllLists = repository.readAllLists
        readAllBooks = readAllFromBookList()

    }

    fun addBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBook(book)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBook(book)
        }
    }

    fun readAllFromBookList(): LiveData<List<Book>> {
        return repository.readAllFromBookList()
    }

    fun readAllFromBookListSync(list: BookList): List<Book> {
        return repository.readAllFromBookListSync(list.name)
    }

    fun readItemFromBooks(row: Int): LiveData<Book> =
        repository.readItemFromBooks(row)

    fun findIdFromUrlAndTitle(url: String, title: String): Int? {
        return repository.readIdFromTitleAndUrl(title, url)
    }

    // Book List stuff
    fun addList(list: BookList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addList(list)
            currentTable = list.name
        }
    }

    fun updateList(list: BookList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateList(list)
            currentTable = list.name
        }
    }

    fun deleteList(list: BookList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteList(list)
            readAllFromBookListSync(list).forEach {
                if (it.bookList == list.name)
                    deleteBook(it)
            }
        }
    }

    fun readList(name: String): BookList {
        return repository.readList(name)
    }

    @Suppress("UNCHECKED_CAST")
    fun readAllLists(): LiveData<List<BookList>> {
        return repository.readAllLists()
    }


    // Configuration stuff

    fun addConfig(config: Config) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addConfig(config)
        }
    }

    fun updateConfig(config: Config) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateConfig(config)
        }
    }

    fun deleteConfig(config: Config) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConfig(config)
        }
    }

    fun readAllFromConfigs(): LiveData<List<Config>> {
        return repository.readAllFromConfigs()
    }

    fun readItemFromConfigs(domain: String): Config? {
        return repository.readItemFromConfigs(domain)
    }

    fun readItemFromConfigs(row_id: Int): Config {
        return repository.readItemFromConfigs(row_id)
    }
}