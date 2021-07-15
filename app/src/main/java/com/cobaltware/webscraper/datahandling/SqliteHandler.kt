package com.cobaltware.webscraper.datahandling

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException
import kotlin.system.exitProcess

lateinit var DB : DataBaseHandler

class DataBaseHandler(context: Context) :
        SQLiteOpenHelper(context, "readerInfo", null, 1)
{
    var currentDB: SQLiteDatabase? = null
    var tableName : String = "BOOKS"

    // Inherited Members to Override
    override fun onUpgrade(fdb: SQLiteDatabase, oldVersion: Int, newVersion: Int){}

    override fun onCreate(db: SQLiteDatabase?) {
        currentDB = db!!
        this.createBookList("BOOKS")
        this.createTable("CONFIG",
                "(COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, DOMAIN VARCHAR(256) NOT NULL, CONTENTXPATH VARCHAR(256) NOT NULL, PREVXPATH VARCHAR(256) NOT NULL, NEXTXPATH VARCHAR(256) NOT NULL)")
        populate()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        if (currentDB != null)
            return currentDB as SQLiteDatabase
        return super.getReadableDatabase()
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        if (currentDB != null)
            return currentDB as SQLiteDatabase
        return super.getWritableDatabase()
    }
    // End of overridden functions
    /**When the database is first created, populate it with some default values*/
    private fun populate()
    {
        // Combine two arrays of strings into a [Map<String, String>]
        fun formatForInsertAction(valArgs: Array<String>) = (arrayOf("DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH") zip valArgs).toMap()

        // Each val array contains a domain contentPath PreviousUrlPath and NextUrlPath
        this.insertItemIntoTable("CONFIG", formatForInsertAction(arrayOf("readnovelfull.com", ".chr-c", ".prev_chap", ".next_chap")))
        this.insertItemIntoTable("CONFIG", formatForInsertAction(arrayOf("royalroad.com", ".chapter-inner", "div.col-md-4:nth-child(1) > a:nth-child(1)", ".col-md-offset-4 > a:nth-child(1)")))
        this.insertItemIntoTable("CONFIG", formatForInsertAction(arrayOf("scribblehub.com", "#chp_raw", "div.prenext > a:nth-child(1)", "div.prenext > a:nth-child(2)")))
        this.insertItemIntoTable("CONFIG", formatForInsertAction(arrayOf("readlightnovel.org", ".hidden", ".prev-link", ".next-link")))
    }
    /**Helper function used to convert a nullable to the default table or a given one*/
    private fun getTableOrGeneric(table: String?) : String = if (!table.isNullOrEmpty()) table else tableName

    /**Read all values in columns given
     * @param table The table to read all the items from
     * @param columns The columns to pull the data from
     * @return A List<List<String>> containing the data from each individual row*/
    fun readAllItems(table: String?, columns: List<String>): List<List<String>>
    {
        val tableData : String = getTableOrGeneric(table)

        val list: MutableList<List<String>> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $tableData"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do
            {
                val line : MutableList<String> = mutableListOf()
                for (column in columns)
                    line.add(result.getString(result.getColumnIndex(column)))

                if (line.size > 1) list.add(line) else list.add(columns)
            }
            while (result.moveToNext())
        }
        result.close()
        return list
    }

    /**Read certain [columns] of data from a value defined by the [row_id]
     * @param table The table to read the item from
     * @param row_id The row id of the given item
     * @return The item's data in a list in the order of the columns*/
    fun readItem(table: String?, row_id: Int, columns: List<String>) : List<String>
    {
        val tableData : String = getTableOrGeneric(table)

        val query = "Select * from $tableData WHERE COL_ID = ?"
        val result = this.readableDatabase.rawQuery(query, arrayOf(row_id.toString()))

        val line : MutableList<String> = mutableListOf()
        if (result.moveToFirst())
        {
            for (column in columns)
                line.add(result.getString(result.getColumnIndex(column)))
        }

        result.close()
        return line
    }

    /**Insert a given item into [table] given its [data]
     * @param table The table to insert the [data] into
     * @param data The data in a Map of <column, value> pairs
     * @return If the operation was successful or not*/
    fun insertItemIntoTable(table: String?, data: Map<String, String>) : Boolean
    {   // Write Line to database
        val tableData : String = getTableOrGeneric(table)

        val db = this.writableDatabase
        // Add values to sql statement
        val values = ContentValues()
        for (entry in data)
            values.put(entry.key, entry.value)

        val success = db.insert(tableData, null, values)

        return success != -1L
    }

    /**Modify an item by its given [row_id] with values defined in [data]
     * @param table The table containing the item to modify
     * @param row_id The unique identifier for the item to modify
     * @param data A map of <column, value> pairs to tell the function what columns and values to modify
     * @return returns true*/
    fun modifyItem(table: String?, row_id: Int, data: Map<String, String>) : Boolean
    {   // Modify line of database
        val tableData : String = getTableOrGeneric(table)

        val db = this.writableDatabase
        val content = ContentValues()
        for (item in data)
            content.put(item.key, item.value)
        db.update(tableData, content, "COL_ID = ?", arrayOf(row_id.toString()))
        return true
    }

    /**Gets the row_id of an item in [table] using the [url] and [title]
     * WARNING: This function can fail if there are identical urls or titles in the same table
     * @param table The table containing the item
     * @param url The url of the item as an identifier
     * @param title The title of the item as an identifier
     * @return The row_id of the given item*/
    fun getIdFromBooklistItem(table: String?, url: String?, title: String?) : Int
    {   // Get col_id of Line
        val tableData : String = getTableOrGeneric(table)
        val db = this.readableDatabase
        val query : String
        val args : String
        if (!url.isNullOrEmpty()){
            query = "SELECT COL_ID FROM $tableData WHERE URL = ?"
            args = url
        }
        else if (!title.isNullOrEmpty()){
            query = "SELECT COL_ID FROM $tableData WHERE NAME = ?"
            args = title
        }
        else {
            // Should fix itself if you try to modify it
            db.execSQL("DELETE FROM $tableName WHERE NAME IS NULL OR trim(NAME) = ''")
            throw IllegalArgumentException("Both title and url are null")
        }

        val cursor = db.rawQuery(query, arrayOf(args))
        if (cursor.count > 1)
        {
            throw IllegalStateException("This function has more than one matching result")
        }

        val index = cursor.getColumnIndex("COL_ID")
        Log.d("Empty?", (!cursor.moveToFirst()).toString())

        if (cursor.moveToFirst() && cursor != null){
            val id = cursor.getInt(index)
            Log.d("Content", id.toString())
            cursor.close()
            return id
        }
        throw exitProcess(0)
    }

    /**Deletes an item from [table] using the [row_id]
     * @param table The table containing the item
     * @param row_id The unique identifier of the item*/
    fun deleteUsingID(table: String?, row_id: Int) =
        this.writableDatabase.let { database ->
            val tableData : String = getTableOrGeneric(table)
            database.delete(tableData, "COL_ID = ?", arrayOf(row_id.toString()))
        }

    /**Checks if the item already exists using its [url]
     * @param table The table to check the item in
     * @param url An identifier used to check if any items already contain that url
     * @return Boolean that tells you if the item exists or not*/
    fun itemAlreadyExists(table: String?, url: String) : Boolean{
        val tableData : String = getTableOrGeneric(table)

        val selection = this.readableDatabase.rawQuery("SELECT * FROM $tableData WHERE URL = ?", arrayOf(url))
        val exists : Boolean = selection.count > 0
        selection.close()
        return exists
    }

    /**Gets a configuration item from a configuration [table] using the [domain] of the website
     * @param table The table containing the configurations to check
     * @param domain The domain of a website that serves as a unique identifier to find a configuration
     * @return A list containing (row_id, domain, contentxpath, prevxpath, nextxpath) of the configuration*/
    fun getConfigFromDomain(table: String, domain: String) : MutableList<String>
    {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM $table WHERE DOMAIN = ?", arrayOf(domain))
        val line : MutableList<String> = mutableListOf()

        fun getStringFromColumnName(colName: String) = result.getString(result.getColumnIndex(colName))

        if (result.moveToFirst())
        {
            line.add(getStringFromColumnName("COL_ID"))
            line.add(getStringFromColumnName("DOMAIN"))
            line.add(getStringFromColumnName("CONTENTXPATH"))
            line.add(getStringFromColumnName("PREVXPATH"))
            line.add(getStringFromColumnName("NEXTXPATH"))
        }
        result.close()
        return line
    }

    /**Gets the tables of the database, except for "android_metadata", "sqlite_sequence", and "CONFIG"*/
    fun getTables() : List<String>
    {
        val result = this.readableDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", arrayOf())
        val returnList : MutableList<String> = mutableListOf()
        // The dropdown item used to add books to the list
        returnList.add("Add a List +")
        if (result.moveToFirst()) {
            do {
                returnList.add(result.getString(result.getColumnIndex("name")))
            } while (result.moveToNext())
        }
        result.close()
        returnList.removeIf { name -> name in listOf("android_metadata", "sqlite_sequence", "CONFIG") }
        return returnList
    }

    /**A function that cleans the table name before creating a table
     * @param name The name of the table to create
     * @param values The sql put in parentheses, look at [createBookList] for example
     * @see createBookList*/
    fun createTable(name: String, values: String)
    {
        val cleanedName = name.replace(" ", "_")
        this.writableDatabase.execSQL("CREATE TABLE IF NOT EXISTS $cleanedName $values")
    }

    /**A simple function that creates a booklist using [createTable]
     * @see createTable*/
    fun createBookList(name: String) = createTable(name, "(COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR(256) NOT NULL, URL VARCHAR(256) NOT NULL)")

    /**Modifies a table's name
     * @param currentTable The current name of the table to change
     * @param name The new name of the table*/
    fun modifyTableName(currentTable: String?, name: String){

        val tableData : String = getTableOrGeneric(currentTable)
        val cleanedName = name.replace(" ", "_")
        this.writableDatabase.execSQL("ALTER TABLE $tableData RENAME TO $cleanedName")
        tableName = name
    }
    /**Deletes a table given its [name]
     * @param name The name of the table to delete*/
    fun deleteTable(name: String) =
        this.writableDatabase.let {
            if (name != "BOOKS")
                it.execSQL("DROP TABLE IF EXISTS $name")
            it.close()
        }
}

