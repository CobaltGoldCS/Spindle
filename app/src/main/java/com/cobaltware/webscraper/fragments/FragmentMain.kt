package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.cobaltware.webscraper.BookAdapter
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.FragmentMainBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.BookViewModel
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import com.cobaltware.webscraper.dialogs.Operations
import com.cobaltware.webscraper.viewcontrollers.MainViewController
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class FragmentMain : Fragment() {

    lateinit var viewController: MainViewController
    lateinit var binding: FragmentMainBinding

    private val dropdownAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater)
        viewController = MainViewController(binding, this)

        DB.currentTable = "Books"
        thread {
            viewController.setupDropdown(dropdownAdapter)
            setObservers()
            setListeners(binding)
        }

        Timer("SettingUp", false).schedule(3000) {
            requireActivity().runOnUiThread { switchBookList(1) }
        }

        return binding.root
    }


    private fun switchBookList() {
        binding.bookLayout.setContent {
            viewController.BookRecycler(
                DB.readAllFromBookList(DB.currentTable),
                textclickHandler = { viewController.initReadFragment(it) },
                buttonClickHandler = { handleBookDialogInit(it) })
        }
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
    }

    /** Sets the actions to take depending on input from the user*/
    private fun setListeners(v: FragmentMainBinding) {

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
        viewController.initAddFragment(book)
    }

    /** Modifies and updates the ui given the position in the [dropdownAdapter]
     * @param position The position of the target [BookList] in the dropdown adapter
     * */
    private fun switchBookList(position: Int) {
        thread {
            Log.d(
                "CONTENTS OF CURRENT TABLE",
                DB.fromBookListSync(BookList(DB.currentTable)).toString()
            )
        }
        onBookListsClick(position)
        switchBookList()
    }

    /**Click handler for the book lists dropdown, changes backend and UI
     * @param position The position of the clicked item */
    private fun onBookListsClick(position: Int) {
        if (position != viewController.view.bookLists.listSelection) {
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