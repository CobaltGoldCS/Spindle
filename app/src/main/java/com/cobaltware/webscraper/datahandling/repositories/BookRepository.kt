package com.cobaltware.webscraper.datahandling.repositories

import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookDao

class BookRepository(private val bookDao: BookDao) {

    suspend fun addBook(book: Book) {
        bookDao.addBook(book)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    suspend fun deleteFromBookList(name: String) {
        bookDao.deleteFromBookList(name)
    }

}