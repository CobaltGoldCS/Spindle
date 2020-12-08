package com.cobaltware.webscraper

import android.content.Intent

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : AppCompatActivity() {
    lateinit var db : DataBaseHandler
    lateinit var bookAdapter : BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide webscraper header
        supportActionBar?.hide()
        // Create database reference
        if (!Python.isStarted())
            Python.start(AndroidPlatform(applicationContext))
        db = DataBaseHandler(applicationContext)
        // Set screen view
        setContentView(R.layout.activity_main)
        // Setting up books from database file
        this.setBooks()
        // Setup add book button
        addMenuButton.setOnClickListener {
            this.onAddMenuClick(null, null)
        }
        // Setup on click for recycler view
        bookLayout.addOnItemTouchListener(RecyclerItemClickListener(this, bookLayout,
                object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                Log.d("DATABASE CONTAINS:", db.readData().joinToString("\n"))
                // Get id of target row
                val id : Int = bookAdapter.bookList[position].col_id
                val data = db.readLine(id)
                // On short click open story
                onBookClick(data[0].toInt(), data[2])
            }
            override fun onItemLongClick(view: View?, position: Int) {
                // on long click, open add menu
                Log.i("DATABASE CONTAINS:", db.readData().joinToString("\n"))
                // Get col id of item clicked
                val id : Int = bookAdapter.bookList[position].col_id
                val data = db.readLine(id)
                // Open AddActivity
                onAddMenuClick(data[1], data[2])
            }
        }))

    }

    private fun setBooks() {
        // Initial Population of the recyclerview
        val lineList = db.readData()
        val bookList = mutableListOf<Book>()
        lineList.forEach{line ->
            val book = Book(line[0].toInt(), line[1], line[2])
            bookList.add(book)
        }
        bookAdapter = BookAdapter(bookList)
        bookLayout.adapter = bookAdapter
        bookLayout.layoutManager = LinearLayoutManager(this)
        bookLayout.setHasFixedSize(true)

    }
    private fun onAddMenuClick(title : String?, url : String?){
        // Setup for switch to AddActivity
        val intent = Intent(applicationContext, AddActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        intent.putExtra("bookList", Wrapper(bookAdapter.bookList.toMutableList()))
        startActivityForResult(intent, 1)
    }

    fun onBookClick(col_id : Int, url : String){
        // Setup for switch to ReadActivity
        val intent = Intent(applicationContext, ReadActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("col_id", col_id)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        // Get book list back from addActivity
        if (resultCode == 1){
            val newList = data?.getParcelableExtra<Wrapper>("newList")!!.bookList
            // Update recyclerview
            bookAdapter.changeItems(newList)
            bookAdapter.notifyDataSetChanged()
        }
    }


}
