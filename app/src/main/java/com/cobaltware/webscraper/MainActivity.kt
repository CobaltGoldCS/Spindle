package com.cobaltware.webscraper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var bookAdapter : BookAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        DB = DataBaseHandler(this) // This is the ONLY Time the database should be changed

        initSimpleUiComponents()
        setupRecyclerView()
        setupDropdown()
    }

    private fun initSimpleUiComponents(){
        // Set ui things
        setContentView(R.layout.activity_main)
        // Setup add book button
        addMenuButton.setOnClickListener { this.onAddMenuClick(null, null) }

        // Hide and show add menu button depending on scroll
        bookLayout.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0 && !addMenuButton.isShown) // Going up on scroll
                    addMenuButton.show()
                else if (dy > 0 && addMenuButton.isShown) // Going down on scroll
                    addMenuButton.hide()
            }
        })
    }
    private fun setupRecyclerView()
    {
        // Initial Population of the recyclerview
        val bookList : MutableList<Book> = changeBooks()
        // With the book adapter initializations, click handlers are also added
        bookAdapter = object : BookAdapter(bookList)
        {
            override fun addClickHandler(col_id: Int)
            {
                Log.i("DATABASE CONTAINS:", DB.readLines().joinToString("\n"))
                val data = DB.readLine(col_id)
                // Open AddActivity
                onAddMenuClick(data[1], data[2])
            }

            override fun openClickHandler(col_id: Int)
            {
                val data = DB.readLine(col_id)
                // On short click open story
                onBookClick(data[0].toInt(), data[2])
            }
        }
        bookLayout.adapter = bookAdapter
        bookLayout.layoutManager = LinearLayoutManager(this)
        bookLayout.setHasFixedSize(true)

    }
    private fun setupDropdown()
    {
        // Set Dropdown menu items and behavior
        val books : MutableList<String> = DB.getTables().toMutableList()
        val dropdownAdapter = ArrayAdapter<String>(this, R.layout.dropdown_tables, books)
        
        bookLists.adapter = dropdownAdapter
        // Fix popup on start bug
        bookLists.setSelection(1, false)

        bookLists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                if (position == 0) // if the selected item is the add book item
                    startPopup(dropdownAdapter, null)
                else {
                    // Update TableName
                    val table = DB.getTables()[position]
                    DB.tableName = table
                    // Update UI (This uses DB.tableName in the function)
                    val newList = changeBooks()
                    bookAdapter.changeItems(newList)
                }
                // Hide changeButton when not in use
                if (position <= 1 && changeButton.visibility == View.VISIBLE)
                    changeButton.visibility = View.INVISIBLE
                else if (position > 1 && changeButton.visibility == View.INVISIBLE)
                    changeButton.visibility = View.VISIBLE
            }
        }
        changeButton.setOnClickListener{
            if (bookLists.selectedItemPosition > 1)
                startPopup(dropdownAdapter, DB.tableName)
        }
    }
    private fun startPopup(adapter : ArrayAdapter<String>, title: String?)
    {
        val menu = ListDialog(this, title)
        menu.setOnDismissListener {
            val newList = DB.getTables()
            adapter.clear()
            adapter.addAll(newList)
            adapter.notifyDataSetChanged()
            // TODO: Fix loop by updating adapter for changing lists instead of invalidation
            // TODO: Efficiency increase with Diffutil
            // https://developer.android.com/reference/android/support/v7/util/DiffUtil
        }
        menu.show()
    }

    private fun changeBooks(): MutableList<Book> {
        val lineList = DB.readLines()
        val bookList = mutableListOf<Book>()
        lineList.forEach{ line ->
            val book = Book(line[0].toInt(), line[1], line[2])
            bookList.add(book)
        }
        return bookList
    }
    private fun onAddMenuClick(title: String?, url: String?)
    {   // Setup for switch to AddActivity
        val intent = Intent(applicationContext, AddActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        intent.putExtra("bookList", ParcelBookList(bookAdapter.bookList.toMutableList()))
        startActivityForResult(intent, 1)
    }

    fun onBookClick(col_id: Int, url: String)
    {
        // Setup for switch to ReadActivity
        val intent = Intent(applicationContext, ReadActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("col_id", col_id)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        // Update book layout view
        if (resultCode == 1){
            val newList = data?.getParcelableExtra<ParcelBookList>("newList")!!.bookList
            bookAdapter.changeItems(newList)
        }
        if (!addMenuButton.isShown)
            addMenuButton.show()
    }
}