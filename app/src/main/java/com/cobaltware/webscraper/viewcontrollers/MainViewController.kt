package com.cobaltware.webscraper.viewcontrollers

import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.FragmentMainBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.fragments.FragmentRead
import com.cobaltware.webscraper.fragments.fragmentTransition

class MainViewController(val view: FragmentMainBinding, private val fragment: Fragment) {

    /**Sets up the book Lists dropdown menu frontend and calls the correct Backend function*/
    fun setupDropdown(dropdownAdapter: ArrayAdapter<String>) =
        fragment.requireActivity().runOnUiThread {
            view.bookLists.setAdapter(dropdownAdapter)
            modifyDropdown()
        }

    /** Updates the book Lists dropdown UI
     * @param selectedItemPosition The position of the selected item in the dropdown
     * */
    fun changeDropdownUI(selectedItemPosition: Int) {
        view.bookLayout.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(fragment.requireContext(), R.anim.booklist_anim)
        view.bookLayout.startLayoutAnimation()

        view.listLayout.weightSum = if (selectedItemPosition <= 1) 4f else 5f
        view.addMenuButton.show()
    }

    /** Initializes a [FragmentRead] for displaying the book in the reader
     * @param book The book to read
     */
    fun initReadFragment(book: Book) {
        fragmentTransition(
            FragmentRead(book),
            View.GONE
        )
    }

    /** Initializes the [ModifyBookDialog] when a book needs to be modified; Note that the fragment is a BottomSheetDialogFragment*/
    fun initAddFragment(book: Book?): ModifyBookDialog = ModifyBookDialog(book).apply {
        show(ReaderApplication.activity.supportFragmentManager, "Add or Change Book")
    }


    /** Modifies the book Lists dropdown menu's gui elements on first run
     *  Also calls the onClick method as well as sets a few parameters for book Lists
     * */
    private fun modifyDropdown() {
        fragment.requireActivity().runOnUiThread {
            view.bookLists.let { dropdown ->
                dropdown.setText(DB.currentTable, false)
                dropdown.listSelection = 1
                dropdown.performClick()
                dropdown.performCompletion()
            }
            view.listLayout.weightSum = 4f
        }
    }

    private fun getColor(res: Int): Color {
        val typedValue = TypedValue()
        val theme = fragment.requireContext().theme
        theme.resolveAttribute(res, typedValue, true)
        return Color(typedValue.data)
    }

    @ExperimentalMaterialApi
    @Composable
    fun BookRecycler(
        data: LiveData<List<Book>>,
        textClickHandler: (Book) -> Unit,
        buttonClickHandler: (Book) -> Unit
    ) {
        val columnData by data.observeAsState()
        columnData?.let { list ->
            BookRecycler(list, textClickHandler, buttonClickHandler)
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BookRecycler(
        list: List<Book>,
        textClickHandler: (Book) -> Unit,
        buttonClickHandler: (Book) -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            items(items = list, itemContent = { item ->
                BookItem(item, textClickHandler, buttonClickHandler)
            })
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun BookItem(
        item: Book,
        textClickHandler: (Book) -> Unit,
        buttonClickHandler: (Book) -> Unit,
    ) {
        ListItem(
            modifier = Modifier
                .clickable { textClickHandler.invoke(item) }
                .padding(top = 5.dp, bottom = 5.dp)
                .border(
                    BorderStroke(2.dp, getColor(R.attr.colorOnPrimary)),
                    RectangleShape
                ),
            text = {
                Text(
                    item.title,
                    fontSize = 20.sp,
                    color = getColor(R.attr.colorOnPrimary),
                )
            },
            trailing = {
                Icon(
                    painterResource(id = R.drawable.icon_menu),
                    "Modify the book",
                    tint = getColor(R.attr.colorPrimary),
                    modifier = Modifier.clickable { buttonClickHandler.invoke(item) }
                )
            }
        )
    }
}