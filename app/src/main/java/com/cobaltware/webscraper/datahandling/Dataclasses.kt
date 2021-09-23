package com.cobaltware.webscraper.datahandling

import androidx.room.Entity
import androidx.room.PrimaryKey


/**The container for a book, corresponds to SQLITE Booklists formats
 * @param row_id The column id of the item (in Sqlite)
 * @param title The title of the book
 * @param url The url where the book is stored*/

@Entity(tableName = "Books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val row_id: Int,
    val title: String,
    val url: String,
    val bookList: String
)

/**The container for a Configuration, corresponds to SQLITE Config Table format
 * @param row_id The column id of the item (in Sqlite)
 * @param domain The domain of the website the config is for
 * @param mainXPath The x path of the content of a book
 * @param prevXPath The x path where the previous url is stored
 * @param nextXPath The x path where the next url is stored*/
@Entity(tableName = "CONFIG")
data class Config(
    @PrimaryKey(autoGenerate = true)
    val row_id: Int,
    val domain: String,
    val mainXPath: String,
    val prevXPath: String,
    val nextXPath: String
)

@Entity(tableName = "Lists")
data class BookList(
    @PrimaryKey
    val name: String

){
    override fun toString(): String {
        return name
    }
}
