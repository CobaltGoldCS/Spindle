package com.cobaltware.webscraper.datahandling


/**The container for a book, corresponds to SQLITE Booklists formats
 * @param col_id The column id of the item (in Sqlite)
 * @param title The title of the book
 * @param url The url where the book is stored*/
data class Book(
    val col_id: Int,
    val title: String,
    val url: String
)

/**The container for a Configuration, corresponds to SQLITE Config Table format
 * @param col_id The column id of the item (in Sqlite)
 * @param domain The domain of the website the config is for
 * @param mainXPath The x path of the content of a book
 * @param prevXPath The x path where the previous url is stored
 * @param nextXPath The x path where the next url is stored*/
data class Config(
    val col_id: Int,
    val domain: String,
    val mainXPath: String,
    val prevXPath: String,
    val nextXPath: String
)
