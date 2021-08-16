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


class FragmentMain() : Fragment() {
    lateinit var bookAdapter: BookAdapter
    lateinit var dropdownAdapter: ArrayAdapter<String>

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
            dropdownAdapter =
                ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())

            viewController.setUI()
            viewController.setupDropdown()
            setObservers()
            setListeners(viewer)
        }

        return viewer
    }

    private fun setObservers() = requireActivity().runOnUiThread {
        val listViewModel: BookViewModel =
            ViewModelProvider(this).get(BookViewModel::class.java)
        listViewModel.readAllLists().observe(viewLifecycleOwner, { bookLists ->
            if (!dropdownAdapter.isEmpty)
                dropdownAdapter.clear()

            bookLists.forEach { list ->
                dropdownAdapter.add(list.name)
            }

            dropdownAdapter.notifyDataSetChanged()
        })

        listViewModel.readAllBooks.observe(
            viewLifecycleOwner,
            { updateBooksContent(it) })
    }

    private fun setListeners(v: View) {
        v.addMenuButton.setOnClickListener { viewController.initAddFragmentDialog(null) }
        v.changeButton.setOnClickListener {
            startPopup(DB.currentTable)
        }
        v.bookLists.setOnItemClickListener { _, _, position, _ ->
            performDropdownItemClick(position)
        }
    }

    private fun performDropdownItemClick(position: Int) {
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
     * @param title The title you want to give to the [ModifyListDialog]*/
    private fun startPopup(title: String?) {
        // DONT MOVE into ViewController, the dismiss
        // listener uses a lot of things from FragmentMain

        val menu = ModifyListDialog(requireContext(), title)
        menu.setOnDismissListener {
            // Make sure that the bookList changes if the item is deleted
            val currentPos = dropdownAdapter.getPosition(DB.currentTable)

            // Navigate to appropriate booklist when a given operation is complete
            when (menu.op) {
                Operations.Delete -> {
                    performDropdownItemClick(1)
                }

                Operations.Nothing -> {}

                // This covers Insert and Update
                else -> {
                    performDropdownItemClick(currentPos)
                }
            }
        }
        menu.show()
    }

}