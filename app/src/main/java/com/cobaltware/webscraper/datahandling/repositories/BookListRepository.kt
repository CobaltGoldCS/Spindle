package com.cobaltware.webscraper.datahandling.repositories

import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.BookListDao

class BookListRepository(private val bookListDao: BookListDao) {

    // Book List stuff
    suspend fun addList(list: BookList) {
        bookListDao.addList(list)
    }

    suspend fun updateList(newName: String, oldName: String) {
        bookListDao.updateList(newName, oldName)
    }

    suspend fun deleteList(list: BookList) {
        bookListDao.deleteList(list)
    }

    fun readAllLists() = bookListDao.readAllLists()

    fun readAllFromBookList(list: String): LiveData<List<Book>> {
        return bookListDao.readAllFromBookList(list)
    }

    fun readAllFromBookListSync(list: String): List<Book> {
        return bookListDao.readAllFromBookListSync(list)
    }
}