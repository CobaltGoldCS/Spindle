package com.cobaltware.webscraper.viewcontrollers

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.BookAdapter
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookViewModel
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.FragmentRead
import com.cobaltware.webscraper.fragments.fragmentTransition
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainViewController(private val view: View, private val fragment: FragmentMain) {


    // Fragment functions
    fun getMainActivity() = fragment.activity as MainActivity

    /**Sets the UI for everything except the dropdown menu, which is set up in [setupDropdown]*/
    fun setUI() = fragment.requireActivity().runOnUiThread {
        view.bookLayout.layoutManager = LinearLayoutManager(fragment.requireContext())
        view.bookLayout.setHasFixedSize(true)
        view.addMenuButton.setOnClickListener { initAddFragmentDialog(null) }

        fragment.bookAdapter = object : BookAdapter() {
            override fun modifyClickHandler(book: Book) =
                initAddFragmentDialog(book)

            override fun openClickHandler(book: Book) =
                initReadFragment(book)
        }
        view.bookLayout.adapter = fragment.bookAdapter
    }

    /**Sets up the [bookLists] dropdown menu frontend and calls the correct Backend function*/
    fun setupDropdown(dropdownAdapter: ArrayAdapter<String>) =
        fragment.requireActivity().runOnUiThread {
            initializeDropdownUI(dropdownAdapter)
            setObservers(dropdownAdapter)
            fragment.modifyDropdown(1)
        }

    /** Set Dropdown menu items and behavior
     * @param initialAdapter The adapter for the [bookLists] dropdown menu
     */
    private fun initializeDropdownUI(initialAdapter: ArrayAdapter<String>) {
        // Set Dropdown menu items and behavior
        view.bookLists.setAdapter(initialAdapter)

        view.changeButton.setOnClickListener {
            fragment.startPopup(
                initialAdapter,
                DB.currentTable
            )
        }
    }

    private fun setObservers(dropdownAdapter: ArrayAdapter<String>) {
        val listViewModel: BookViewModel =
            ViewModelProvider(fragment).get(BookViewModel::class.java)
        listViewModel.readAllLists().observe(fragment.viewLifecycleOwner, { bookLists ->
            if (!dropdownAdapter.isEmpty)
                dropdownAdapter.clear()

            bookLists.forEach { list ->
                dropdownAdapter.add(list.name)
            }

            dropdownAdapter.notifyDataSetChanged()
        })

        listViewModel.readAllBooks.observe(
            fragment.viewLifecycleOwner,
            { fragment.updateBooksContent(it) })
    }

    /** Updates the [bookLists] dropdown UI
     * @param selectedItemPosition The position of the selected item in the dropdown
     * */
    fun changeDropdownUI(selectedItemPosition: Int) {
        view.bookLayout.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(fragment.requireContext(), R.anim.booklist_anim)
        view.bookLayout.startLayoutAnimation()

        fragment.listLayout.weightSum = if (selectedItemPosition <= 1) 4f else 5f
        fragment.addMenuButton.show()
    }

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