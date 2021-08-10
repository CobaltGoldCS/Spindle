package com.cobaltware.webscraper.datahandling

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cobaltware.webscraper.datahandling.*

class BookRepository(private val bookDao: BookDao, private val configDao: ConfigDao) {

    val readAllConfigs: LiveData<List<Config>> = configDao.readAllFromConfigs()
    val readAllLists: LiveData<List<BookList>> = bookDao.readAllLists()

    suspend fun addBook(book: Book){
        bookDao.addBook(book)
    }

    suspend fun updateBook(book: Book){
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    fun readAllFromBookList(): LiveData<List<Book>>{
        return bookDao.readAllFromBookList()
    }

    fun readAllFromBookListSync(list: String): List<Book>{
        return bookDao.readAllFromBookListSync(list)
    }

    fun readItemFromBooks(row: Int): LiveData<Book>{
        return bookDao.readItemFromBooks(row)
    }

    fun readIdFromTitleAndUrl(title: String, url: String): Int?{
        return bookDao.readIdFromTitleAndUrl(title, url)
    }

    // Book List stuff
    suspend fun addList(list: BookList){
        bookDao.addList(list)
    }

    suspend fun updateList(list: BookList){
        bookDao.updateList(list)
        readAllFromBookListSync(list.name).forEach {
            it.bookList = list.name
            bookDao.updateBook(it)
        }
    }

    suspend fun deleteList(list: BookList){
        bookDao.deleteList(list)
        bookDao.deleteFromBookList(list.name)
    }

    fun readList(name: String) : BookList{
        return bookDao.readList(name)
    }

    fun readAllLists(): LiveData<List<BookList>>{
        return bookDao.readAllLists()
    }


    // CONFIGURATIONS
    suspend fun addConfig(config: Config){
        configDao.addConfig(config)
    }

    suspend fun updateConfig(config: Config){
        configDao.updateConfig(config)
    }

    suspend fun deleteConfig(config: Config){
        configDao.deleteConfig(config)
    }

    fun readAllFromConfigs(): LiveData<List<Config>> =
        configDao.readAllFromConfigs()


    fun readItemFromConfigs(domain: String) =
        try{
            configDao.readItemFromConfigs(domain)[0]
        } catch (e: IndexOutOfBoundsException){
            null
        }

    fun readItemFromConfigs(row_id: Int) =
        configDao.readItemFromConfigs(row_id)[0]
}