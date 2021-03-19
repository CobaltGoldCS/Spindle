package com.cobaltware.webscraper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainFragment : Fragment() {
    lateinit var bookAdapter : BookAdapter
    var bookList : MutableList<Book>? = null
    var viewer : View? = null

    companion object {
        @JvmStatic
        fun newInstance(BookList: List<Book>?): MainFragment {
            val myFragment = MainFragment()
            myFragment.bookList = BookList?.toMutableList()
            return myFragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewer = inflater.inflate(R.layout.activity_main, container, false)

        initSimpleUiComponents(viewer!!)
        setupRecyclerView(viewer!!)
        setupDropdown(viewer!!)

        return viewer!!
    }

    fun changeItems(list : List<Book>) {bookAdapter.changeItems(list)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(requireContext()))
        DB = DataBaseHandler(requireContext()) // This is the ONLY Time the database should be changed
    }


    private fun initSimpleUiComponents(v : View){
        // Set ui things
        // Setup add book button
        v.addMenuButton.setOnClickListener { this.initAddActivity(null, null) }

        // Hide and show add menu button depending on scroll
        v.bookLayout.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0 && !v.addMenuButton.isShown) // Going up on scroll
                {
                    v.addMenuButton.show()
                } else if (dy > 0 && v.addMenuButton.isShown) // Going down on scroll
                {
                    v.addMenuButton.hide()
                }
            }
        })
    }

    private fun setupRecyclerView(v : View)
    {
        // Initial Population of the recyclerview
        if (bookList == null)
            bookList = changeBooks()
        // With the book adapter initializations, click handlers are also added
        bookAdapter = object : BookAdapter(bookList!!)
        {
            override fun addClickHandler(col_id: Int)
            {
                Log.i("DATABASE CONTAINS:", DB.readAllBooklistItems(null).joinToString("\n"))
                val data = DB.readBooklistItem(null, col_id)
                // Open AddActivity
                initAddActivity(data[1], data[2])
            }

            override fun openClickHandler(col_id: Int)
            {
                val data = DB.readBooklistItem(null, col_id)
                // On short click open story
                initReadActivity(data[0].toInt(), data[2])
            }
        }
        v.bookLayout.adapter = bookAdapter
        v.bookLayout.layoutManager = LinearLayoutManager(requireContext())
        v.bookLayout.setHasFixedSize(true)

    }

    private fun setupDropdown(v : View)
    {
        // Set Dropdown menu items and behavior
        val books : MutableList<String> = DB.getTables().toMutableList()
        val dropdownAdapter = ArrayAdapter<String>(requireContext(), R.layout.dropdown_item, books)

        v.bookLists.setAdapter(dropdownAdapter)
        // Fix popup on start bug

        v.bookLists.setOnItemClickListener { _, _, position, _ ->
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
                v.changeButton.visibility = View.INVISIBLE
            else if (position > 1 && changeButton.visibility == View.INVISIBLE)
                v.changeButton.visibility = View.VISIBLE
        }
        v.changeButton.setOnClickListener{
            if (books.indexOf(bookLists.text.toString()) > 1)
                startPopup(dropdownAdapter, DB.tableName)
        }
    }
    private fun startPopup(adapter: ArrayAdapter<String>, title: String?)
    {
        val menu = ListDialog(requireContext(), title)
        menu.setOnDismissListener {
            val newList = DB.getTables()
            adapter.clear()
            adapter.addAll(newList)
            adapter.notifyDataSetChanged()
        }
        menu.show()
    }

    private fun changeBooks(): MutableList<Book> {
        // Change Books to ones in the given table
        val lineList = DB.readAllBooklistItems(null)
        val bookList = mutableListOf<Book>()
        lineList.forEach{ line ->
            val book = Book(line[0].toInt(), line[1], line[2])
            bookList.add(book)
        }
        return bookList
    }
    private fun initAddActivity(title: String?, url: String?)
    {   // Setup for switch to AddActivity
        val fragmentTrans = requireFragmentManager().beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, AddFragment.newInstance(bookList!!.toList(), url, title))
        fragmentTrans.commit()
    }

    fun initReadActivity(col_id: Int, url: String)
    {
        // Setup for switch to ReadActivity
        val fragmentTrans = requireFragmentManager().beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, ReadFragment.newInstance(url, col_id))
        fragmentTrans.commit()
    }


}