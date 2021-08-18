package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cobaltware.webscraper.BookAdapter
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.BookViewModel
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import com.cobaltware.webscraper.dialogs.Operations
import com.cobaltware.webscraper.viewcontrollers.MainViewController
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlin.concurrent.thread


class FragmentMain : Fragment() {
    lateinit var bookAdapter: BookAdapter
    private lateinit var dropdownAdapter: ArrayAdapter<String>

    lateinit var viewController: MainViewController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val viewer = inflater.inflate(R.layout.fragment_main, container, false)!!
        viewController = MainViewController(viewer, this)

        thread {
            initializeAdapters()

            viewController.setUI(bookAdapter)
            viewController.setupDropdown(dropdownAdapter)
            setObservers()
            setListeners(viewer)
        }

        return viewer
    }

    private fun initializeAdapters() {
        dropdownAdapter =
            ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())
        bookAdapter = object : BookAdapter() {
            override fun modifyClickHandler(book: Book) =
                viewController.initAddFragmentDialog(book)

            override fun openClickHandler(book: Book) =
                viewController.initReadFragment(book)
        }
    }

    private fun setObservers() = requireActivity().runOnUiThread {
        val listViewModel: BookViewModel =
            ViewModelProvider(this).get(BookViewModel::class.java)
        listViewModel.readAllLists().observe(viewLifecycleOwner, { bookLists ->
            if (!dropdownAdapter.isEmpty)
                dropdownAdapter.clear()

            bookLists.forEach { list -> dropdownAdapter.add(list.name) }
            dropdownAdapter.notifyDataSetChanged()
        })

        listViewModel.readAllBooks.observe(viewLifecycleOwner, { updateBooksContent(it) })
    }

    private fun setListeners(v: View) {
        v.addMenuButton.setOnClickListener { viewController.initAddFragmentDialog(null) }
        v.changeButton.setOnClickListener { startPopup(DB.currentTable) }
        v.bookLists.setOnItemClickListener { _, _, position, _ -> switchBookList(position) }
    }

    private fun switchBookList(position: Int) {
        onBookListsClick(position)
        viewController.modifyDropdown(position)
        thread {
            Log.d(
                "CONTENTS OF CURRENT TABLE",
                DB.fromBookListSync(BookList(DB.currentTable)).toString()
            )
        }
        bookAdapter.changeItems(DB.fromBookListSync(BookList(DB.currentTable)))
        bookAdapter.notifyDataSetChanged()
    }

    /**Update the content of the recyclerview using a list of books
     * @param books The list of books to populate the adapter with*/
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

    /**Click handler for the [bookLists] dropdown, changes backend and UI
     * @param position The position of the clicked item */
    private fun onBookListsClick(
        position: Int,
        updateBookList: Boolean = false
    ) {
        if (position != bookLists.listSelection) {
            DB.currentTable = dropdownAdapter.getItem(position)!!

            if (updateBookList)
                thread {
                    val books = DB.fromBookListSync(BookList(DB.currentTable))
                    updateBooksContent(books)
                }
            if (position == 0) { // if the selected item is the add book item
                startPopup(null)
                return
            }

            viewController.changeDropdownUI(position)
        }
    }

    /** Initializes a [ModifyListDialog], used for making and changing lists
     * @param title The title of the bookList for the [ModifyListDialog]*/
    private fun startPopup(title: String?) {
        val menu = ModifyListDialog(requireContext(), title)
        menu.setOnDismissListener {
            val currentPos = dropdownAdapter.getPosition(DB.currentTable)
            // Navigates to relevant bookLists depending on the operation executed by the menu
            when (menu.op) {
                Operations.Delete, Operations.Nothing -> {
                    switchBookList(1)
                }
                Operations.Insert, Operations.Update -> {
                    switchBookList(currentPos)
                }
            }
        }
        menu.show()
    }

}