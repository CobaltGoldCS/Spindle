package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.*
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.BookViewModel
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import com.cobaltware.webscraper.dialogs.Operations
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.util.*
import kotlin.concurrent.thread


class FragmentMain() : Fragment() {
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val viewer = inflater.inflate(R.layout.fragment_main, container, false)!!

        thread {
            setComplexClassParameters(viewer)
            setupDropdown(viewer)
            requireActivity().runOnUiThread { setUI(viewer) }
        }

        return viewer
    }

    /** Sets complex and long class parameters that would over complicate the [onCreateView] method*/
    private fun setComplexClassParameters(viewer: View) {

        // With the book adapter initializations, click handlers are also added
        bookAdapter = object : BookAdapter() {
            override fun modifyClickHandler(book: Book) =
                initAddFragmentDialog(book)

            override fun openClickHandler(book: Book) =
                initReadFragment(book)
        }
        viewer.bookLayout.adapter = bookAdapter

    }

    /**Sets the UI for everything except the dropdown menu, which is set up in [setupDropdown]*/
    private fun setUI(v: View) {
        v.bookLayout.layoutManager = LinearLayoutManager(requireContext())
        v.bookLayout.setHasFixedSize(true)
        v.addMenuButton.setOnClickListener { initAddFragmentDialog(null) }

        val listViewModel: BookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        listViewModel.readAllBooks.observe(viewLifecycleOwner, { updateBooksContent(it) })
    }

    private fun updateBooksContent(books: List<Book>) {
        Log.d("CURRENT TABLE", DB.currentTable)

        val bookList = mutableListOf<Book>()
        for (book in books) {
            if (book.bookList.equals(DB.currentTable, ignoreCase = true))
                bookList.add(book)
        }
        requireActivity().runOnUiThread {
            bookAdapter.changeItems(bookList)
            bookAdapter.notifyDataSetChanged()
        }
    }

    /**Sets up the [bookLists] dropdown menu backend components and calls the UI initializer*/
    private fun setupDropdown(v: View) {
        val dropdownAdapter =
            ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())

        requireActivity().runOnUiThread { initializeDropdownUI(v, dropdownAdapter) }

        modifyDropdown(1)
    }

    /** Set Dropdown menu items and behavior
     * @param v The [View] for the function to reference
     * @param initialAdapter The adapter for the [bookLists] dropdown menu
     */
    private fun initializeDropdownUI(
        v: View,
        initialAdapter: ArrayAdapter<String>
    ) {
        // Set Dropdown menu items and behavior
        v.bookLists.setAdapter(initialAdapter)
        v.bookLists.setOnItemClickListener { _, _, position, _ ->

            onBookListsClick(position, initialAdapter, v)
            modifyDropdown(position)
            thread {
                Log.d(
                    "CONTENTS OF CURRENT TABLE",
                    DB.readAllFromBookListSync(BookList(DB.currentTable)).toString()
                )
            }
            bookAdapter.changeItems(DB.readAllFromBookListSync(BookList(DB.currentTable)))
            bookAdapter.notifyDataSetChanged()
        }


        v.changeButton.setOnClickListener { // If it isn't the add book item or books
            startPopup(
                initialAdapter,
                DB.currentTable
            )
        }
        val listViewModel: BookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        listViewModel.readAllLists().observe(viewLifecycleOwner, {
            if (!initialAdapter.isEmpty)
                initialAdapter.clear()
            it.forEach { list ->
                initialAdapter.add(list.name)
            }
            initialAdapter.notifyDataSetChanged()
        })
        initialAdapter.notifyDataSetChanged()
    }

    /**Click handler for the [bookLists] dropdown, changes backend and UI
     * @param position The position of the clicked item
     * @param dropdownAdapter The adapter connected to [bookLists]
     * @param v The [View] used to reference the UI elements*/
    private fun onBookListsClick(
        position: Int,
        dropdownAdapter: ArrayAdapter<String>,
        v: View,
        updateBookList: Boolean = false
    ) {
        if (position != v.bookLists.listSelection) {
            DB.currentTable = dropdownAdapter.getItem(position)!!

            if (updateBookList)
                thread {
                    val books = DB.readAllFromBookListSync(BookList(DB.currentTable))
                    updateBooksContent(books)
                }
            onDropdownItemClick(position, dropdownAdapter)
            changeDropdownUI(v, position)
        }
    }

    /** Handler used for when a book list is clicked in the dropdown [bookLists]
     * @param position The position that was clicked in the dropdown
     * @param dropdownAdapter The adapter used for the dropdown menu
     * */
    private fun onDropdownItemClick(
        position: Int,
        dropdownAdapter: ArrayAdapter<String>,
    ) {
        if (position == 0) { // if the selected item is the add book item
            startPopup(dropdownAdapter, null)
            return
        }
    }

    /** Updates the [bookLists] dropdown UI
     * @param v The view used to reference the UI elements
     * @param selectedItemPosition The position of the selected item in the dropdown
     * */
    private fun changeDropdownUI(v: View, selectedItemPosition: Int) {
        v.bookLayout.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.booklist_anim)
        v.bookLayout.startLayoutAnimation()

        listLayout.weightSum = if (selectedItemPosition <= 1) 4f else 5f
        addMenuButton.show()
    }

    /** Modifies the [bookLists] dropdown menu's gui elements depending upon the [position] given.
     *  Also calls the onClick method as well as sets a few parameters for [bookLists]
     * @param position The current position in the drop down
     * */
    private fun modifyDropdown(position: Int) {
        thread {
            requireActivity().runOnUiThread {
                bookLists.setText(DB.currentTable, false)
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
            // Make sure that the bookList changes if the item is deleted
            val currentPos = adapter.getPosition(DB.currentTable)
            // TODO: Fix renaming/updating mechanism
            // TODO: Implement this _when_ so that it navigates properly
            when (menu.op) {
                Operations.Delete -> {
                    onBookListsClick(1, adapter, requireView())
                }
                Operations.Insert -> {
                    onBookListsClick(currentPos, adapter, requireView(), true)
                }
                Operations.Update -> thread {
                    val books = DB.readAllFromBookListSync(BookList(DB.currentTable))
                    updateBooksContent(books)
                    modifyDropdown(currentPos)
                }
                else -> {
                }
            }
        }
        menu.show()
    }


    // Fragment functions
    private fun getMainActivity() = activity as MainActivity

    /** Initializes a dialog for adding books using [ModifyBookDialog]
     * @param book The book to modify, null will add a book
     */
    private fun initAddFragmentDialog(book: Book?) {
        val menu = ModifyBookDialog.newInstance(book)
        menu.show(getMainActivity().supportFragmentManager, "Add or Change Book")

    }

    /** Initializes a [FragmentRead] for displaying the book in the reader
     * @param book The book to read
     */
    private fun initReadFragment(book: Book) {
        fragmentTransition(
            getMainActivity(),
            FragmentRead.newInstance(book),
            View.GONE
        )
    }


}