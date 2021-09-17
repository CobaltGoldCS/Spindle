package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
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
        MainViewController(this)
    }

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                var selectedItem by remember { mutableStateOf(DB.currentTable) }
                // Modify list open items
                var title by remember { mutableStateOf("") }
                val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }

                WebscraperTheme {
                    viewController.ModifyListDialog(title,
                        changeList = {selectedItem = it; DB.currentTable = it; title = selectedItem},
                        open = modifyListOpen,  dismissState = setModifyListOpen)
                    Column {
                        LiveDropdown(items = DB.readAllLists){ items ->
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
                                    Row(
                                        Modifier
                                            .padding(horizontal = 5.dp)
                                            .fillMaxWidth(if (items.indexOf(BookList(selectedItem)) > 1) .85f else 1f)
                                            .clickable { // Anchor view
                                                expanded = !expanded
                                            },
                                        horizontalArrangement = Arrangement.Center
                                    ) { // Anchor view
                                        Text(selectedItem.toString(), Modifier.align(Alignment.CenterVertically), fontSize =  30.sp, color = MaterialTheme.colors.onPrimary)
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            null,
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            tint = MaterialTheme.colors.onPrimary
                                        )
                                    }
                                    if (items.indexOf(BookList(selectedItem)) > 1) {
                                        OutlinedButton(modifier = Modifier.fillMaxWidth().padding(all = 2.dp), onClick = {
                                            setModifyListOpen(true)
                                        }) {
                                            Icon(imageVector = Icons.Filled.MenuOpen, null)
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = {expanded = !expanded},
                                    modifier = Modifier.align(Alignment.TopCenter)) {
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
                        // This is the main content
                        Scaffold(
                            content = {
                                LiveRecycler(DB.readAllFromBookList(selectedItem)) { list: List<Book> ->
                                    items(list) { book ->
                                        viewController.BookItem(
                                            book.title,
                                            { viewController.initReadFragment(book) },
                                            { viewController.initAddFragment(book) }
                                        )
                                    }
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                            floatingActionButton = {
                                FloatingActionButton(
                                    modifier = Modifier.padding(end = 300.dp),
                                    onClick = { viewController.initAddFragment(null) },
                                    content = { Icon(imageVector = Icons.Filled.Add, null) },
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onSecondary
                                )
                            },
                        )
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
        WebscraperTheme {
            Text(
                item.toString(),
                Modifier,
                if (item.toString() == selectedItem) {
                    MaterialTheme.colors.primary
                } else{ MaterialTheme.colors.onPrimary }
            )
        }
    }

}