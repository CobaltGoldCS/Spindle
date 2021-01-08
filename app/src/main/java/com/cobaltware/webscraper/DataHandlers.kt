package com.cobaltware.webscraper

import android.content.ContentValues
import android.content.Context

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import android.util.Log

import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import android.os.Parcelable

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, "readerInfo", null,
        1)
{
    var tableName : String = "Books"

    override fun onCreate(db: SQLiteDatabase?) { db?.execSQL("CREATE TABLE IF NOT EXISTS BOOKS (COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR(256), URL VARCHAR(256))") }

    override fun onUpgrade(fdb: SQLiteDatabase, oldVersion: Int, newVersion: Int){}
    fun readData(): MutableList<List<String>>
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
                line.add(result.getString(result.getColumnIndex("NAME")))
                line.add(result.getString(result.getColumnIndex("URL")))
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
    fun getId(url : String, title : String) : Int
    {   // Get col_id of Line
        val db = this.readableDatabase
        val query : String
        val args : String
        if (url.isNotEmpty()){
            query = "SELECT COL_ID FROM BOOKS WHERE URL = ?"
            args = url
        }
        else{
            query = "SELECT COL_ID FROM BOOKS WHERE NAME = ?"
            args = title
        }

        val result = db.rawQuery(query, arrayOf(args))
        result.close()
        Log.d("Not Empty?", result.moveToFirst().toString())
        Log.d("Content", result.getColumnName(0))
        return result.getInt(0)
    }
    fun delete(id : Int){
        val db = this.writableDatabase
        db.delete(tableName, "COL_ID = ?", arrayOf(id.toString()))
    }
    fun checkDuplicate(url : String) : Boolean{
        val db = this.readableDatabase
        val selection = db.rawQuery("SELECT * FROM $tableName WHERE URL = ?", arrayOf(url))
        selection.close()
        return selection.count > 0
    }
    fun getTables() : List<String>
    {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", arrayOf())
        val returnList : MutableList<String> = mutableListOf<String>()
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
}
@Parcelize
data class Book(val col_id : Int, val title: String, val url : String) : Parcelable
@Parcelize
data class Wrapper(val bookList : @RawValue MutableList<Book>) : Parcelable

