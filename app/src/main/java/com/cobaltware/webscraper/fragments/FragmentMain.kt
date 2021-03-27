package com.cobaltware.webscraper.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cobaltware.webscraper.*
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.DB
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlin.concurrent.thread

class FragmentMain : Fragment() {
    lateinit var bookAdapter : BookAdapter
    lateinit var viewer : View
    var bookList : MutableList<Book>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewer = inflater.inflate(R.layout.fragment_main, container, false)!!
        thread {
            setupRecyclerView(viewer)
            initSimpleUiComponents(viewer)
            setupDropdown(viewer)
            setColors(viewer)
        }

        return viewer
    }

    private fun initSimpleUiComponents(v : View){requireActivity().runOnUiThread {
        // Set ui things
        v.bookLists.setText(R.string.defaultVal)
        v.changeButton.visibility = View.INVISIBLE
        // Setup add book button
        v.addMenuButton.setOnClickListener { this.initAddFragment(null, null) }

        // Hide and show add menu button depending on scroll
        v.bookLayout.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, downX : Int, downY: Int) {
                when
                {
                    // Going up on scroll
                    downY < 0 && !v.addMenuButton.isShown -> v.addMenuButton.show()
                    // Going down on scroll
                    downY > 0 && v.addMenuButton.isShown -> v.addMenuButton.hide()
                }
            }
        })
    }
    }

    private fun setupRecyclerView(v : View)
    {thread {
        // Initial Population of the recyclerview
        bookList = changeBooks()
        // With the book adapter initializations, click handlers are also added
        bookAdapter = object : BookAdapter(bookList!!) {
            override fun addClickHandler(col_id: Int) {
                Log.i("DATABASE CONTAINS:",
                    DB.readAllItems(null, listOf<String>("COL_ID", "NAME", "URL"))
                        .joinToString("\n")
                )
                val data = DB.readItem(null, col_id, listOf<String>("COL_ID", "NAME", "URL"))
                // Open AddActivity
                initAddFragment(data[1], data[2])
            }

            override fun openClickHandler(col_id: Int) {
                val data = DB.readItem(null, col_id, listOf<String>("COL_ID", "NAME", "URL"))
                // On short click open story
                initReadFragment(data[0].toInt(), data[2])
            }
        }
        // Initial Recycler change needed for sync properly
        val table = DB.getTables()[1]
        DB.tableName = table
        val newList = changeBooks()

        requireActivity().runOnUiThread{
            v.bookLayout.adapter = bookAdapter
            v.bookLayout.layoutManager = LinearLayoutManager(requireContext())
            v.bookLayout.setHasFixedSize(true)

            // Ui changes for initial recycler change
            v.bookLayout.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.booklist_anim)
            v.bookLayout.startLayoutAnimation()
            bookAdapter.changeItems(newList)
        }

    }
    }
    private fun setColors(v : View)
    {
        val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val backgroundColor : Int = if (darkMode) R.color.background else Color.WHITE
        val context  = requireContext()

        requireActivity().runOnUiThread {
            v.bookLayout.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
            v.listLayout.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
            // Been having issues with setting the selection
            v.bookLists.listSelection = 1
        }
    }

    private fun setupDropdown(v : View)
    {thread{
        // Set Dropdown menu items and behavior
        val books : MutableList<String> = DB.getTables().toMutableList()
        val dropdownAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_dropdown, books)

        requireActivity().runOnUiThread{
            v.bookLists.setAdapter(dropdownAdapter)
            v.bookLists.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) // if the selected item is the add book item
                    startPopup(dropdownAdapter, null)
                else {
                    // Update TableName
                    val table = DB.getTables()[position]
                    if (table == DB.tableName) // If the selected item has been clicked again
                        return@setOnItemClickListener
                    DB.tableName = table
                    // Update UI (This uses DB.tableName in the function)
                    v.bookLayout.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.booklist_anim)
                    val newList = changeBooks()
                    v.bookLayout.startLayoutAnimation()
                    bookAdapter.changeItems(newList)
                }
                // Hide changeButton when not in use
                if (position <= 1 && v.changeButton.visibility == View.VISIBLE)
                    v.changeButton.visibility = View.INVISIBLE
                else if (position > 1 && v.changeButton.visibility == View.INVISIBLE)
                    v.changeButton.visibility = View.VISIBLE
            }
            v.changeButton.setOnClickListener{
                if (books.indexOf(v.bookLists.text.toString()) > 1)
                    startPopup(dropdownAdapter, DB.tableName)
            }
        }
    }
    }
    private fun startPopup(adapter: ArrayAdapter<String>, title: String?)
    {
        val menu = ListDialog(requireContext(), title)
        menu.setOnDismissListener {
            // Update adapter on dismiss
            val newList = DB.getTables()
            adapter.clear()
            adapter.addAll(newList)
            adapter.notifyDataSetChanged()
        }
        menu.show()
    }

    private fun changeBooks(): MutableList<Book> {
        // Change Books to ones in the given table
        val lineList = DB.readAllItems(null, listOf<String>("COL_ID", "NAME", "URL"))
        val bookList = mutableListOf<Book>()
        lineList.forEach{ line ->
            val book = Book(line[0].toInt(), line[1], line[2])
            bookList.add(book)
        }
        return bookList
    }
    // Change fragments
    private fun initAddFragment(title: String?, url: String?) {
        val activity : MainActivity = activity as MainActivity
        fragmentTransition(activity, FragmentAdd.newInstance(bookList!!.toList(), url, title), View.GONE)
    }

    private fun initReadFragment(col_id: Int, url: String)    {
        val activity : MainActivity = activity as MainActivity
        fragmentTransition(activity, FragmentRead.newInstance(url, col_id), View.GONE)
    }


}