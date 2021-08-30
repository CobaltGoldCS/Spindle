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
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import com.cobaltware.webscraper.dialogs.Operations
import com.cobaltware.webscraper.viewcontrollers.MainViewController
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlin.concurrent.thread


class FragmentMain : Fragment() {

    lateinit var viewController: MainViewController


    private val bookAdapter: BookAdapter by lazy {
        object : BookAdapter(viewController) {
            override fun modifyClickHandler(book: Book) {
                handleBookDialogInit(book)
            }
        }
    }
    private val dropdownAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val viewer = inflater.inflate(R.layout.fragment_main, container, false)!!
        viewController = MainViewController(viewer, this)

        thread {
            viewController.setUI(bookAdapter)
            viewController.setupDropdown(dropdownAdapter)
            setObservers()
            setListeners(viewer)
        }

        return viewer
    }

    /** Sets the handlers that automatically modify ui elements as the database changes */
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

    /** Sets the actions to take depending on input from the user*/
    private fun setListeners(v: View) {
        v.addMenuButton.setOnClickListener {
            handleBookDialogInit(null)
        }
        v.changeButton.setOnClickListener { startPopup(DB.currentTable) }
        v.bookLists.setOnItemClickListener { _, _, position, _ -> switchBookList(position) }
    }

    /** Handles the [ModifyBookDialog] details
     * @see ModifyBookDialog
     * @param book The book to be passed to the dialog
     * */
    private fun handleBookDialogInit(book: Book?) {
        val menu = viewController.initAddFragment(book)
        menu.addDismissListener {
            when (menu.op) {
                Operations.Update, Operations.Insert -> {
                    // Menu position doesn't count add/change book list, so we need to add one to account for that
                    switchBookList(menu.position + 1)
                }
                else -> {
                    Log.i("Book", "No insert or update of book")
                }
            }
        }
    }

    /** Modifies and updates the ui given the position in the [dropdownAdapter]
     * @param position The position of the target [BookList] in the dropdown adapter
     * */
    private fun switchBookList(position: Int) {
        val newList = DB.fromBookListSync(BookList(DB.currentTable))
        thread {
            Log.d(
                "CONTENTS OF CURRENT TABLE",
                newList.toString()
            )
        }
        onBookListsClick(position)
        viewController.modifyDropdown(position)
        bookAdapter.changeItems(newList)
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
        requireActivity().runOnUiThread { bookAdapter.changeItems(bookList) }
    }

    /**Click handler for the [bookLists] dropdown, changes backend and UI
     * @param position The position of the clicked item */
    private fun onBookListsClick(position: Int) {
        if (position != bookLists.listSelection) {
            DB.currentTable = dropdownAdapter.getItem(position)!!

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