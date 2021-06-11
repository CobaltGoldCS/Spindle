package com.cobaltware.webscraper.datahandling

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlin.system.exitProcess

lateinit var DB : DataBaseHandler

class DataBaseHandler(context: Context) :
        SQLiteOpenHelper(context, "readerInfo", null, 1)
{
     var currentDB: SQLiteDatabase? = null

    // Inherited Members to Override

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
    override fun onUpgrade(fdb: SQLiteDatabase, oldVersion: Int, newVersion: Int){}

    var tableName : String = "BOOKS"
    private fun getTableOrGeneric(table: String?) : String = if (!table.isNullOrEmpty()) table else tableName

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
    fun readItem(table: String?, COL_ID: Int, columns: List<String>) : List<String>
    {
        val tableData : String = getTableOrGeneric(table)

        val db = this.readableDatabase

        val query = "Select * from $tableData WHERE COL_ID = ?"
        val result = db.rawQuery(query, arrayOf(COL_ID.toString()))

        val line : MutableList<String> = mutableListOf()
        if (result.moveToFirst())
        {
            for (column in columns)
                line.add(result.getString(result.getColumnIndex(column)))
        }

        result.close()
        return line
    }
    fun insertItemIntoTable(table: String?, map: Map<String, String>) : Boolean
    {   // Write Line to database
        val tableData : String = getTableOrGeneric(table)

        val db = this.writableDatabase
        // Add values to sql statement
        val values = ContentValues()
        for (entry in map)
            values.put(entry.key, entry.value)

        val success = db.insert(tableData, null, values)

        return success != -1L
    }
    fun modifyItem(table: String?, col_id: Int, map: Map<String, String>) : Boolean
    {   // Modify line of database
        val tableData : String = getTableOrGeneric(table)

        val db = this.writableDatabase
        val content = ContentValues()
        for (item in map)
            content.put(item.key, item.value)
        db.update(tableData, content, "COL_ID = ?", arrayOf(col_id.toString()))
        return true
    }
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

    fun deleteUsingID(table: String?, id: Int) =
        this.writableDatabase.let { database ->
            val tableData : String = getTableOrGeneric(table)
            database.delete(tableData, "COL_ID = ?", arrayOf(id.toString()))
        }
    fun checkDuplicateBooklist(table: String?, url: String) : Boolean{
        val db = this.readableDatabase
        val tableData : String = getTableOrGeneric(table)

        val selection = db.rawQuery("SELECT * FROM $tableData WHERE URL = ?", arrayOf(url))
        val isValid : Boolean = selection.count > 0
        selection.close()
        return isValid
    }
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
    // Table Related functions
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
    fun createTable(name: String, values: String)
    {
        // Values (String) : The sql put in parentheses, look at [createBooklist] for example
        val cleanedName = name.replace(" ", "_")
        val db = this.writableDatabase
        db.execSQL("CREATE TABLE IF NOT EXISTS $cleanedName $values")
    }
    fun createBookList(name: String) = createTable(name, "(COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR(256) NOT NULL, URL VARCHAR(256) NOT NULL)")
    fun modifyTable(currentTable: String?, name: String){

        val tableData : String = getTableOrGeneric(currentTable)
        val cleanedName = name.replace(" ", "_")
        val db = this.writableDatabase

        db.execSQL("ALTER TABLE $tableData RENAME TO $cleanedName")
        tableName = name
    }
    fun deleteTable(name: String) =
        this.writableDatabase.let {
            if (name != "BOOKS")
                it.execSQL("DROP TABLE IF EXISTS $name")
            it.close()
        }
}

