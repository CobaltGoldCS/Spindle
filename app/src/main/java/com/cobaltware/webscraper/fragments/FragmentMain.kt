package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.*
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.DB
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlin.concurrent.thread


class FragmentMain : Fragment() {
    lateinit var viewer: View

    var bookList: MutableList<Book>? = null
    lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewer = inflater.inflate(R.layout.fragment_main, container, false)!!

        thread {
            setComplexClassParameters()
            setupDropdown(viewer)
            requireActivity().runOnUiThread { setUI(viewer) }
        }

        return viewer
    }

    /** Sets complex and long class parameters that would over complicate the [onCreateView] method*/
    private fun setComplexClassParameters() {
        // Initial Population of the recyclerview
        bookList = obtainBooks()

        // With the book adapter initializations, click handlers are also added
        bookAdapter = object : BookAdapter(bookList!!) {
            override fun modifyClickHandler(col_id: Int) {
                val data = DB.readItem(null, col_id, listOf("NAME", "URL"))
                // Open AddActivity
                initAddFragmentDialog(data[0], data[1])
            }

            override fun openClickHandler(col_id: Int) {
                val data = DB.readItem(null, col_id, listOf("COL_ID", "URL"))
                // On short click open book
                initReadFragment(data[0].toInt(), data[1])
            }
        }
    }

    /**Sets the UI for everything except the dropdown menu, which is set up in [setupDropdown]*/
    private fun setUI(v: View) {
        v.bookLayout.adapter = bookAdapter
        v.bookLayout.layoutManager = LinearLayoutManager(requireContext())
        v.bookLayout.setHasFixedSize(true)
        v.addMenuButton.setOnClickListener { initAddFragmentDialog(null, null) }
    }

    /**Sets up the [bookLists] dropdown menu backend components and calls the UI initializer*/
    private fun setupDropdown(v: View) {
        val books: MutableList<String> = DB.getTables().toMutableList()
        val dropdownAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, books)

        requireActivity().runOnUiThread { initializeDropdownUI(v, books, dropdownAdapter) }

        modifyDropdown(dropdownAdapter.getPosition(DB.tableName))
    }

    /** Set Dropdown menu items and behavior
     * @param v The [View] for the function to reference
     * @param dropdownAdapter The adapter for the [bookLists] dropdown menu
     * @param tables A list of all the book lists contained in the database
     */
    private fun initializeDropdownUI(
        v: View,
        tables: List<String>,
        dropdownAdapter: ArrayAdapter<String>
    ) {
        // Set Dropdown menu items and behavior
        v.bookLists.setAdapter(dropdownAdapter)
        v.bookLists.setOnItemClickListener { _, _, position, _ ->
            onBookListsClick(position, dropdownAdapter, v)
        }
        v.changeButton.setOnClickListener {
            if (tables.indexOf(v.bookLists.text.toString()) > 1) // If it isn't the add book item
                startPopup(dropdownAdapter, DB.tableName)
        }
    }

    /**Click handler for the [bookLists] dropdown, changes backend and UI
     * @param position The position of the clicked item
     * @param dropdownAdapter The adapter connected to [bookLists]
     * @param v The [View] used to reference the UI elements*/
    private fun onBookListsClick(position: Int, dropdownAdapter: ArrayAdapter<String>, v: View) {
        val selectedTable = DB.getTables()[position]
        if (selectedTable != DB.tableName) {
            onDropdownItemClick(position, dropdownAdapter, selectedTable)
            changeDropdownUI(v, position)
        }
    }

    /** Handler used for when a book list is clicked in the dropdown [bookLists]
     * @param position The position that was clicked in the dropdown
     * @param dropdownAdapter The adapter used for the dropdown menu
     * @param selectedTable The string representation for the selected table name
     * */
    private fun onDropdownItemClick(
        position: Int,
        dropdownAdapter: ArrayAdapter<String>,
        selectedTable: String
    ) {
        if (position == 0) { // if the selected item is the add book item
            startPopup(dropdownAdapter, null)
            return
        }
        // Update TableName
        DB.tableName = selectedTable
    }

    /** Updates the [bookLists] dropdown UI
     * @param v The view used to reference the UI elements
     * @param selectedItemPosition The position of the selected item in the dropdown
     * */
    private fun changeDropdownUI(v: View, selectedItemPosition: Int) {
        val newList = obtainBooks()
        v.bookLayout.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.booklist_anim)
        v.bookLayout.startLayoutAnimation()
        bookAdapter.changeItems(newList)

        listLayout.weightSum = if (selectedItemPosition == 1) 4f else 5f
        addMenuButton.show()
    }

    /** Modifies the [bookLists] dropdown menu's gui elements depending upon the [position] given.
     *  Also calls the onClick method as well as sets a few parameters for [bookLists]
     * @param position The current position in the drop down
     * */
    private fun modifyDropdown(position: Int) {
        thread {
            requireActivity().runOnUiThread {
                bookLists.setText(DB.tableName, false)
                bookLists.listSelection = position
                bookLists.callOnClick()
                bookLists.performCompletion()

                listLayout.weightSum = if (position == 1) 4f else 5f
            }
        }
    }

    /** Initializes a [ModifyListDialog], used for making and changing lists
     * @param adapter The [bookLists]' adapter, used for modifying the list
     * @param title The title you want to give to the [ModifyListDialog]*/
    private fun startPopup(adapter: ArrayAdapter<String>, title: String?) {
        val menu = ModifyListDialog(requireContext(), title)
        menu.setOnDismissListener {
            // Update adapter on dismiss
            val newList = DB.getTables()
            adapter.clear()
            adapter.addAll(newList)
            adapter.notifyDataSetChanged()

            // Make sure that the bookList changes if the item is deleted
            if (menu.deleted) onBookListsClick(1, adapter, viewer)
        }
        menu.show()
    }


    /** Obtain books from current table, defined as [DB.tableName]
     * @return list of [Book] from the current table open*/
    private fun obtainBooks(): MutableList<Book> {
        val lineList = DB.readAllItems(null, listOf("COL_ID", "NAME", "URL"))
        val bookList = mutableListOf<Book>()
        lineList.forEach { line ->
            val (colId, title, url) = line
            val book = Book(colId.toInt(), title, url)
            bookList.add(book)
        }
        return bookList
    }

    // Fragment functions
    private fun getMainActivity() = activity as MainActivity

    /** Initializes a dialog for adding books using [ModifyBookDialog]
     * @param title The title of the book
     * @param url The url where the book content is stored
     */
    private fun initAddFragmentDialog(title: String?, url: String?) {
        val menu = ModifyBookDialog.newInstance(bookAdapter, url, title)
        menu.show(getMainActivity().supportFragmentManager, "Add or Change Book")
    }

    /** Initializes a [FragmentRead] for displaying the book in the reader
     * @param col_id The column id where the book is stored in the database
     * @param url The url where the book content is stored
     */
    private fun initReadFragment(col_id: Int, url: String) {
        fragmentTransition(getMainActivity(), FragmentRead.newInstance(url, col_id), View.GONE)
    }


}