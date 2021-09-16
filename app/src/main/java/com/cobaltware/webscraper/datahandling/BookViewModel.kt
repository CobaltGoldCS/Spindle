package com.cobaltware.webscraper.datahandling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BookViewModel(application: Application) :
    AndroidViewModel(application) {
    var currentTable: String = "Books"
        set(value) {
            field = value
            readAllBooks = readAllFromBookList(value)
        }

    val readAllConfigs: LiveData<List<Config>>
    var readAllBooks: LiveData<List<Book>>
        private set
    var readAllLists: LiveData<List<BookList>>
    private val repository: BookRepository


    init {
        val database = BookDatabase.getDatabase(application)
        val bookDao = database.bookDao()
        val configDao = database.configDao()

        repository = BookRepository(bookDao, configDao)
        readAllConfigs = repository.readAllConfigs
        readAllLists = repository.readAllLists
        readAllBooks = readAllFromBookList(currentTable)

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

    fun readAllFromBookList(list: String): LiveData<List<Book>> {
        return repository.readAllFromBookList(list)
    }

    fun fromBookListSync(list: BookList): List<Book> {
        return repository.fromBookListSync(list.name)
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

    fun updateList(newName: String, oldName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateList(newName, oldName)
            fromBookListSync(BookList(oldName)).forEach {
                it.bookList = newName
                repository.updateBook(it)
            }
        }
        currentTable = newName
    }

    fun deleteList(list: BookList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteList(list)
            fromBookListSync(list).forEach {
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