package com.cobaltware.webscraper.datahandling

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM Books")
    fun readAllFromBookListSync(): List<Book>

    @Query("SELECT * FROM Books WHERE bookList = :name")
    fun fromBookListSync(name: String): List<Book>

    @Query("DELETE FROM Books WHERE bookList = :name")
    suspend fun deleteFromBookList(name: String)

    @Query("SELECT * FROM Books WHERE row_id = :row")
    fun readItemFromBooks(row: Int): LiveData<Book>

    @Query("SELECT row_id FROM Books WHERE title = :title AND url = :url")
    fun readIdFromTitleAndUrl(title: String, url: String): Int?

}

@Dao
interface ConfigDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addConfig(config: Config)

    @Update
    suspend fun updateConfig(config: Config)

    @Delete
    suspend fun deleteConfig(config: Config)

    @Query("SELECT * FROM CONFIG")
    fun readAllFromConfigs(): LiveData<List<Config>>

    @Query("SELECT * FROM CONFIG WHERE row_id = :id")
    fun readItemFromConfigs(id: Int): List<Config>

    @Query("SELECT * FROM CONFIG WHERE domain = :domain")
    fun readItemFromConfigs(domain: String): List<Config>
}

@Dao
interface BookListDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addList(list: BookList)

    @Query("UPDATE Lists SET name= :newName WHERE name = :oldName")
    suspend fun updateList(newName: String, oldName: String)

    @Delete
    suspend fun deleteList(list: BookList)

    @Query("SELECT * FROM Lists WHERE name = :name")
    fun readList(name: String): BookList

    @Query("SELECT * FROM Lists")
    fun readAllLists(): LiveData<List<BookList>>

    @Query("SELECT * FROM Books WHERE bookList = :name")
    fun readAllFromBookList(name: String): LiveData<List<Book>>

    @Query("SELECT * FROM Books WHERE bookList = :name")
    fun readAllFromBookListSync(name: String): List<Book>
}