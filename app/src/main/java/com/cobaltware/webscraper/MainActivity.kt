package com.cobaltware.webscraper

import android.content.Intent

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : AppCompatActivity() {
    lateinit var db : DataBaseHandler
    lateinit var bookAdapter : BookAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // Hide web scraper header
        supportActionBar?.hide()
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(applicationContext))
        db = DataBaseHandler(applicationContext)
        // Set screen view
        setContentView(R.layout.activity_main)
        // Setting up books from database file
        this.setBooks()
        // Setup add book button
        addMenuButton.setOnClickListener { this.onAddMenuClick(null, null) }

        // Set Dropdown menu items and behavior
        val books : MutableList<String> = db.getTables().toMutableList()
        bookLists.adapter = ArrayAdapter<String>(this, R.layout.dropdown_tables, books)

        bookLists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view : View?, position : Int, id : Long)
            {
                val table = db.getTables()[position]
                db.tableName = table
            }
        }
    }

    private fun setBooks()
    {
        // Initial Population of the recyclerview
        val lineList = db.readData()
        val bookList = mutableListOf<Book>()
        lineList.forEach{line ->
            val book = Book(line[0].toInt(), line[1], line[2])
            bookList.add(book)
        }
        // With the book adapter initializations, click handlers are also added
        bookAdapter = object : BookAdapter(bookList)
        {
            override fun addClickHandler(col_id : Int)
            {
                Log.i("DATABASE CONTAINS:", db.readData().joinToString("\n"))
                val data = db.readLine(col_id)
                // Open AddActivity
                onAddMenuClick(data[1], data[2])
            }

            override fun openClickHandler(col_id: Int)
            {
                val data = db.readLine(col_id)
                // On short click open story
                onBookClick(data[0].toInt(), data[2])
            }
        }
        bookLayout.adapter = bookAdapter
        bookLayout.layoutManager = LinearLayoutManager(this)
        bookLayout.setHasFixedSize(true)

    }
    private fun onAddMenuClick(title : String?, url : String?)
    {   // Setup for switch to AddActivity
        val intent = Intent(applicationContext, AddActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        intent.putExtra("bookList", Wrapper(bookAdapter.bookList.toMutableList()))
        intent.putExtra("database_TableName", db.tableName)
        startActivityForResult(intent, 1)
    }

    fun onBookClick(col_id : Int, url : String)
    {
        // Setup for switch to ReadActivity
        val intent = Intent(applicationContext, ReadActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("col_id", col_id)
        intent.putExtra("database_TableName", db.tableName)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        // Update book layout view
        if (resultCode == 1){
            val newList = data?.getParcelableExtra<Wrapper>("newList")!!.bookList
            bookAdapter.changeItems(newList)
            bookAdapter.notifyDataSetChanged()
        }
    }
}