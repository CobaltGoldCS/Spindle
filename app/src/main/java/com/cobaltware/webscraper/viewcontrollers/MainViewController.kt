package com.cobaltware.webscraper.viewcontrollers

import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.BookAdapter
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.FragmentRead
import com.cobaltware.webscraper.fragments.fragmentTransition
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainViewController(private val view: View, private val fragment: FragmentMain) {


    // Fragment functions
    private fun getMainActivity() = fragment.activity as MainActivity

    /**Sets the UI for everything except the dropdown menu, which is set up in [setupDropdown]*/
    fun setUI() = fragment.requireActivity().runOnUiThread {
        view.bookLayout.layoutManager = LinearLayoutManager(fragment.requireContext())
        view.bookLayout.setHasFixedSize(true)

        fragment.bookAdapter = object : BookAdapter() {
            override fun modifyClickHandler(book: Book) =
                initAddFragmentDialog(book)

            override fun openClickHandler(book: Book) =
                initReadFragment(book)
        }
        view.bookLayout.adapter = fragment.bookAdapter
    }

    /**Sets up the [bookLists] dropdown menu frontend and calls the correct Backend function*/
    fun setupDropdown() =
        fragment.requireActivity().runOnUiThread {
            view.bookLists.setAdapter(fragment.dropdownAdapter)
            modifyDropdown(1)
        }

    /** Updates the [bookLists] dropdown UI
     * @param selectedItemPosition The position of the selected item in the dropdown
     * */
    fun changeDropdownUI(selectedItemPosition: Int) {
        view.bookLayout.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(fragment.requireContext(), R.anim.booklist_anim)
        view.bookLayout.startLayoutAnimation()

        view.listLayout.weightSum = if (selectedItemPosition <= 1) 4f else 5f
        view.addMenuButton.show()
    }

    /** Initializes a dialog for adding books using [ModifyBookDialog]
     * @param book The book to modify, null will add a book
     */
    fun initAddFragmentDialog(book: Book?) {
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


    /** Modifies the [bookLists] dropdown menu's gui elements depending upon the [position] given.
     *  Also calls the onClick method as well as sets a few parameters for [bookLists]
     * @param position The current position in the drop down
     * */
    fun modifyDropdown(position: Int) {
        // Technically should be moved to view controller
        fragment.requireActivity().runOnUiThread {
            view.bookLists.let { dropdown ->
                dropdown.setText(DB.currentTable, false)
                dropdown.listSelection = position
                dropdown.callOnClick()
                dropdown.performCompletion()
            }
            view.listLayout.weightSum = if (position == 1) 4f else 5f
        }
    }
}