package com.cobaltware.webscraper

import android.content.ContentValues
import android.content.Context

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import android.util.Log

import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import android.os.Parcelable
import kotlin.system.exitProcess

lateinit var DB : DataBaseHandler
class DataBaseHandler(context: Context) : SQLiteOpenHelper(context, "readerInfo", null,
        1)
{
    var tableName : String = "BOOKS"

    override fun onCreate(db: SQLiteDatabase?) { this.createBookList("BOOKS") }

    override fun onUpgrade(fdb: SQLiteDatabase, oldVersion: Int, newVersion: Int){}
    fun readLines(): MutableList<List<String>>
    {
        val list: MutableList<List<String>> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $tableName"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do
            {
                val line : MutableList<String> = mutableListOf<String>()
                line.add(result.getString(result.getColumnIndex("COL_ID")))
                line.add(result.getString(result.getColumnIndex("NAME"  )))
                line.add(result.getString(result.getColumnIndex("URL"   )))
                list.add(line)
            }
            while (result.moveToNext())
        }
        result.close()
        return list
    }
    fun readLine(COL_ID : Int) : List<String>
    {
        val db = this.readableDatabase
        val query = "Select * from $tableName WHERE COL_ID = ?"
        val result = db.rawQuery(query, arrayOf(COL_ID.toString()))
        val line : MutableList<String> = mutableListOf<String>()
        if (result.moveToFirst())
        {
            line.add(result.getString(result.getColumnIndex("COL_ID")))
            line.add(result.getString(result.getColumnIndex("NAME"  )))
            line.add(result.getString(result.getColumnIndex("URL"   )))
        }

        result.close()
        return line
    }
    fun writeLine(name : String, url : String) : Boolean
    {   // Write Line to database
        val db = this.writableDatabase
        val values = ContentValues()
        
        values.put("NAME", name)
        values.put("URL", url)
        val success = db.insert(tableName, null, values)
        db.close()
        return success != -1L
    }
    fun modify(col_id : Int, url : String, name: String?) : Boolean
    {   // Modify line of database
        val db = this.writableDatabase
        val content = ContentValues()
        if (name != null)
            content.put("NAME", name)
        content.put("URL", url)
        db.update(tableName, content, "COL_ID = ?", arrayOf(col_id.toString()))
        return true
    }
    fun getId(url : String?, title : String) : Int
    {   // Get col_id of Line
        val db = this.readableDatabase
        val query : String
        val args : String
        if (!url.isNullOrEmpty()){
            query = "SELECT COL_ID FROM $tableName WHERE URL = ?"
            args = url
        }
        else{
            query = "SELECT COL_ID FROM $tableName WHERE NAME = ?"
            args = title
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
    fun delete(id : Int){
        val db = this.writableDatabase
        db.delete(tableName, "COL_ID = ?", arrayOf(id.toString()))
    }
    fun checkDuplicate(url : String) : Boolean{
        val db = this.readableDatabase
        val selection = db.rawQuery("SELECT * FROM $tableName WHERE URL = ?", arrayOf(url))
        val isValid : Boolean = selection.count > 0
        selection.close()
        return isValid
    }
    // Table Related functions
    fun getTables() : List<String>
    {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", arrayOf())
        val returnList : MutableList<String> = mutableListOf<String>()
        // The dropdown item used to add books to the list
        returnList.add("Add a List +")
        if (result.moveToFirst()) {
            do {
                returnList.add(result.getString(result.getColumnIndex("name")))
            } while (result.moveToNext())
        }
        result.close()
        returnList.removeIf { name ->
            name == "android_metadata" || name == "sqlite_sequence" }
        return returnList
    }
    fun createBookList(name : String)
    {
        val db = this.writableDatabase
        db.execSQL("CREATE TABLE IF NOT EXISTS $name (COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR(256), URL VARCHAR(256))")
    }
    fun modifyBookList(name : String){
        val db = this.writableDatabase
        db.execSQL("ALTER TABLE $tableName RENAME TO $name")
    }
    fun deleteBookList(name: String)
    {
        val db = this.writableDatabase
        if (name != "BOOKS")
            db.execSQL("DROP TABLE IF EXISTS $name")
        db.close()
    }
}

@Parcelize
data class Book(val col_id : Int, val title: String, val url : String) : Parcelable
@Parcelize
data class ParcelBookList(val bookList : @RawValue MutableList<Book>) : Parcelable

