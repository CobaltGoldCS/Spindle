package com.cobaltware.webscraper.datahandling

import androidx.lifecycle.LiveData

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

    fun readAllFromBookList(list: String): LiveData<List<Book>>{
        return bookDao.readAllFromBookList(list)
    }

    fun readAllFromBookListSync(): List<Book>{
        return bookDao.readAllFromBookListSync()
    }

    fun fromBookListSync(list: String): List<Book>{
        return bookDao.fromBookListSync(list)
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

    suspend fun updateList(newName: String, oldName: String){
        bookDao.updateList(newName, oldName)
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