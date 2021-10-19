package com.cobaltware.webscraper.datahandling

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cobaltware.webscraper.datahandling.migrations.migrations


@Database(
    entities = [Book::class, Config::class, BookList::class],
    version = 4,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun configDao(): ConfigDao
    abstract fun bookListDao(): BookListDao

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
                ).addMigrations(*migrations)
                    .allowMainThreadQueries()
                    .createFromAsset("defaultDatabase.db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}