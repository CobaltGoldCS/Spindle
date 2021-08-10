package com.cobaltware.webscraper.datahandling

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.nio.file.Path


@Database(
    entities = [Book::class, Config::class, BookList::class],
    version = 3,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null


        fun getDatabase(context: Context): BookDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "readerInfo"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .allowMainThreadQueries()
                    .createFromAsset("defaultDatabase.db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        val result = database.query(
            "SELECT name FROM sqlite_master WHERE type='table'",
            arrayOf()
        )
        database.execSQL("CREATE TABLE `newBooksHash` (row_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, url TEXT NOT NULL, bookList TEXT NOT NULL)")
        database.execSQL("CREATE TABLE `listHash` (`name` TEXT PRIMARY KEY NOT NULL)")

        // Populate List
        if (result.moveToFirst()) {
            do {
                val name = result.getString(result.getColumnIndex("name"))
                Log.d("Name", name)
                if (!isFieldExist(database, name, "URL"))
                    continue
                val insertVal = ContentValues()
                insertVal.put("name", name)
                database.insert("listHash", OnConflictStrategy.IGNORE, insertVal)

            } while (result.moveToNext())
        }
        Log.d("Contents of list", result.columnNames.joinToString(", "))
        result.close()
        database.execSQL("DROP TABLE IF EXISTS `Lists`")
        database.execSQL("ALTER TABLE `listHash` RENAME TO `Lists`")

        // UPDATE BOOKS
        val rows = database.query(
            "SELECT * FROM Lists"
        )
        if (rows.moveToFirst())
            do {
                val name = rows.getString(rows.getColumnIndex("name"))
                Log.d("REQUESTING DATA FROM", name)
                val innerRows =
                    database.query("SELECT * FROM $name")
                Log.d(
                    "Columns within $name",
                    innerRows.columnNames.joinToString(",")
                )
                if (innerRows.moveToFirst())
                    do {
                        val tempVals = ContentValues()
                        Log.d("Name", innerRows.getString(0))

                        tempVals.put("title", innerRows.getString(innerRows.getColumnIndex("NAME")))
                        tempVals.put("url", innerRows.getString(innerRows.getColumnIndex("URL")))
                        tempVals.put("bookList", rows.getString(rows.getColumnIndex("name")))

                        database.insert("newBooksHash", OnConflictStrategy.ABORT, tempVals)
                    } while (innerRows.moveToNext())
                innerRows.close()
                database.execSQL("DROP TABLE IF EXISTS ${rows.getString(rows.getColumnIndex("name"))}")
            } while (rows.moveToNext())
        rows.close()

        database.execSQL("DROP TABLE IF EXISTS `Books`")
        database.execSQL("ALTER TABLE `newBooksHash` RENAME TO `Books`")

        // Update CONFIG
        database.execSQL("CREATE TABLE `new_CONFIGS` (row_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, domain TEXT NOT NULL, mainXPath TEXT NOT NULL, prevXPath TEXT NOT NULL, nextXPath TEXT NOT NULL)")
        moveItemsToOtherTable(
            database,
            "CONFIG",
            "new_CONFIGS",
            listOf("COL_ID", "DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH"),
            listOf("row_id", "domain", "mainXPath", "prevXPath", "nextXPath")
        )
        database.execSQL("DROP TABLE IF EXISTS CONFIG")
        database.execSQL("ALTER TABLE `new_CONFIGS` RENAME TO `CONFIG`")

    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `listHash` (`name` TEXT PRIMARY KEY NOT NULL)")
        val innerRows =
            database.query("SELECT * FROM Lists")

        val values = ContentValues()
        values.put("name", "Add A BookList")
        database.insert("listHash", OnConflictStrategy.REPLACE, values)

        if (innerRows.moveToFirst())
            do {
                val tempVals = ContentValues()
                Log.d("Name", innerRows.getString(0))

                tempVals.put("name", innerRows.getString(0))
                database.insert("listHash", OnConflictStrategy.REPLACE, tempVals)
            } while (innerRows.moveToNext())
        innerRows.close()
        database.execSQL("DROP TABLE Lists")
        database.execSQL("ALTER TABLE `listHash` RENAME TO `Lists`")

    }
}


fun moveItemsToOtherTable(
    database: SupportSQLiteDatabase,
    oldTable: String,
    newTable: String,
    original_Names: List<String>,
    new_names: List<String>
) {
    val rows = database.query("SELECT * FROM $oldTable")
    if (rows.moveToFirst())
        do {
            val tempVals = ContentValues()
            for (i in original_Names.indices) {
                tempVals.put(new_names[i], rows.getString(rows.getColumnIndex(original_Names[i])))
            }

            database.insert(newTable, OnConflictStrategy.ABORT, tempVals)
        } while (rows.moveToNext())

    rows.close()
}

fun isFieldExist(database: SupportSQLiteDatabase, tableName: String, fieldName: String): Boolean {
    var isExist = false
    val res = database.query("PRAGMA table_info($tableName)", null)
    res.moveToFirst()
    do {
        val currentColumn: String = res.getString(1)
        if (currentColumn == fieldName) {
            isExist = true
            break
        }
    } while (res.moveToNext())
    res.close()
    return isExist
}
