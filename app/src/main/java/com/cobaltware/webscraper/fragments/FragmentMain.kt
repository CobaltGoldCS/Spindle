package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
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
                val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }
                var modifyListText by remember { mutableStateOf<String?>(selectedItem) }


                WebscraperTheme {
                    viewController.ModifyListDialog(modifyListText,
                        changeList = {selectedItem = it; DB.currentTable = it; modifyListText = selectedItem},
                        open = modifyListOpen,  dismissState = setModifyListOpen)
                    Column {
                        LiveDropdown(items = DB.readAllLists){ items ->

                            var expanded by remember { mutableStateOf(false) }
                            var dropDownWidth by remember { mutableStateOf(0) }
                            var dropDownHeight by remember { mutableStateOf(0)}

                            Column(Modifier,  horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedTextField(value = selectedItem.toString(),
                                        onValueChange = {},
                                        readOnly = true,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(textColor = MaterialTheme.colors.onPrimary),
                                        modifier = Modifier
                                            .fillMaxWidth(if (items.indexOf(BookList(selectedItem)) > 1) .85f else 1f)
                                            .padding(start = 5.dp, end = 5.dp)
                                            .onSizeChanged {
                                                // Workaround to get exact height and width of dropdown at runtime
                                                dropDownWidth = it.width
                                                dropDownHeight = it.height
                                            },
                                        label = {
                                            Text("Book Lists",modifier = Modifier
                                                .clickable { expanded = !expanded })
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowDropDown,
                                                null,
                                                modifier = Modifier
                                                    .clickable { expanded = !expanded }
                                                    .align(Alignment.CenterVertically),
                                                tint = MaterialTheme.colors.onPrimary
                                            )
                                        }
                                    )
                                    if (items.indexOf(BookList(selectedItem)) > 1)
                                        OutlinedButton(onClick = { setModifyListOpen(true) }, modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, end = 5.dp)
                                            .height(with(LocalDensity.current) { dropDownHeight.toDp() - 8.dp })
                                        ) {
                                            Icon(imageVector = Icons.Filled.MenuOpen, null)
                                        }
                                }
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = {expanded = !expanded},
                                        modifier = Modifier.width(with(LocalDensity.current){dropDownWidth.toDp()})
                                    ) {
                                        items.forEach {
                                            DropdownMenuItem(onClick = {
                                                // Handle On Click when dropdown item is pressed
                                                if (items.indexOf(it) == 0) {
                                                    modifyListText = null
                                                    setModifyListOpen(true)
                                                }
                                                else {
                                                    selectedItem = it.name
                                                    modifyListText = selectedItem
                                                    DB.currentTable = selectedItem
                                                }
                                                expanded = !expanded
                                            }, modifier = Modifier.fillMaxWidth()) {
                                                DropdownItem(it, selectedItem)
                                            }
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
        selectedItem: String,
        modifier: Modifier = Modifier
    ) {
        WebscraperTheme {
            Text(
                item.toString(),
                modifier,
                if (item.toString() == selectedItem) {
                    MaterialTheme.colors.primary
                } else{ MaterialTheme.colors.onPrimary }
            )
        }
    }

}