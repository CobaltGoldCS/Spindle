package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.FragmentMainBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import com.cobaltware.webscraper.dialogs.Operations
import com.cobaltware.webscraper.viewcontrollers.*
import kotlin.concurrent.thread


class FragmentMain : Fragment() {

    private val viewController by lazy {
        MainViewController(binding, this)
    }
    private lateinit var binding: FragmentMainBinding

    private val dropdownAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())
    }

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater)
        thread {
            //viewController.setupDropdown(dropdownAdapter)
            //setObservers()
            //setListeners(binding)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                var selectedItem by remember { mutableStateOf(DB.currentTable) }
                // Modify list open items
                var title by remember {mutableStateOf("")}
                val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }

                viewController.ModifyListDialog(title,
                    changeList = {selectedItem = it; DB.currentTable = it; title = selectedItem},
                    open = modifyListOpen,  dismissState = setModifyListOpen)
                Column() {
                    LiveDropdown(items = DB.readAllLists){ items ->
                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            // Top row
                            Row(Modifier.fillMaxWidth()){
                                Row(
                                    Modifier
                                        .padding(5.dp, 0.dp)
                                        .fillMaxWidth(.85f)
                                        .clickable { // Anchor view
                                            expanded = !expanded
                                        }) { // Anchor view
                                    Text(selectedItem.toString(), Modifier, getColor(R.attr.colorOnPrimary, context), 20.sp)
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        null,
                                        tint = getColor(R.attr.colorOnPrimary, context)
                                    )
                                }
                                if (items.indexOf(BookList(selectedItem)) > 1) {
                                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                        setModifyListOpen(true)
                                    }) {
                                        Icon(imageVector = Icons.Filled.MenuOpen, null, tint = getColor(R.attr.colorOnPrimary, context))
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {expanded = !expanded}) {
                                items.forEach {
                                    DropdownMenuItem(onClick = {
                                        // Handle On Click when dropdown item is pressed
                                        if (items.indexOf(it) == 0) {
                                            title = ""
                                            setModifyListOpen(true)
                                        }
                                        else {
                                            selectedItem = it.name
                                            title = selectedItem
                                            DB.currentTable = selectedItem
                                        }
                                        expanded = !expanded
                                    }) {
                                        DropdownItem(it, selectedItem)
                                    }
                                }
                            }
                        }
                    }
                    LiveRecycler(DB.readAllFromBookList(selectedItem)){ list: List<Book> ->
                        items(list) { book ->
                            viewController.BookItem(
                                book.title,
                                {viewController.initReadFragment(book)},
                                {viewController.initAddFragment(book)}
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ComposeView.DropdownItem(
        item: BookList,
        selectedItem: String
    ) {
        Text(
            item.toString(),
            Modifier,
            if (item.name == selectedItem) getColor(
                R.attr.colorPrimaryVariant,
                context
            ) else getColor(R.attr.colorOnPrimary, context)
        )
    }

    /** Initializes a [ModifyListDialog], used for making and changing lists
     * @param title The title of the bookList for the [ModifyListDialog]*/
    private fun startPopup(title: String?, onDismiss: (Operations) -> Unit = {}) {
        val menu = ModifyListDialog(requireContext(), title)
        menu.setOnDismissListener {
            // Navigates to relevant bookLists depending on the operation executed by the menu
            onDismiss(menu.op)
        }
        menu.show()
    }

}