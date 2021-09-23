package com.cobaltware.webscraper.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.viewcontrollers.LiveDropdown
import com.cobaltware.webscraper.viewcontrollers.LiveRecycler
import com.cobaltware.webscraper.viewcontrollers.WebscraperTheme
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch


class FragmentMain : Fragment() {

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var selectedItem by remember { mutableStateOf(DB.currentTable) }
                // Modify list open items
                val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }
                var modifyListText by remember { mutableStateOf<String?>(selectedItem) }


                WebscraperTheme {
                    ModifyListDialog(
                        modifyListText,
                        changeList = {
                            selectedItem = it
                            DB.currentTable = it
                            modifyListText = selectedItem
                        },
                        open = modifyListOpen, dismissState = setModifyListOpen
                    )
                    Column {
                        val recyclerState = rememberLazyListState()
                        LiveDropdown(items = DB.readAllLists) { items ->

                            var expanded by remember { mutableStateOf(false) }
                            var dropDownSize by remember { mutableStateOf(IntSize(0, 0)) }

                            Column(Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    ClickableOutlinedText(
                                        text = selectedItem, expanded = expanded,
                                        modifier = Modifier
                                            .fillMaxWidth(if (items.indexOf(BookList(selectedItem)) > 1) .85f else 1f)
                                            .padding(start = 5.dp, end = 5.dp)
                                            // Workaround to get exact height and width of dropdown at runtime
                                            .onSizeChanged { dropDownSize = it }
                                            .clickable { expanded = !expanded },
                                    )

                                    if (items.indexOf(BookList(selectedItem)) > 1)
                                        OutlinedButton(
                                            onClick = { setModifyListOpen(true) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, end = 5.dp)
                                                .height(with(LocalDensity.current) { dropDownSize.height.toDp() - 8.dp })
                                        ) {
                                            Icon(imageVector = Icons.Filled.MenuOpen, null)
                                        }
                                }
                                Spacer(Modifier.height(10.dp))

                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = !expanded },
                                        modifier = Modifier
                                            .width(with(LocalDensity.current) { dropDownSize.width.toDp() })
                                    ) {
                                        val coroutine = rememberCoroutineScope()
                                        items.forEach {
                                            DropdownMenuItem(onClick = {
                                                // Handle On Click when dropdown item is pressed
                                                coroutine.launch {
                                                    if (items.indexOf(it) == 0) {
                                                        modifyListText = null
                                                        setModifyListOpen(true)
                                                    } else {
                                                        selectedItem = it.name
                                                        modifyListText = selectedItem
                                                        DB.currentTable = selectedItem
                                                        recyclerState.scrollToItem(0)
                                                    }
                                                    expanded = !expanded
                                                }
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
                                LiveRecycler(
                                    DB.readAllFromBookList(selectedItem),
                                    recyclerState
                                ) { list: List<Book> ->
                                    items(list) { book ->
                                        BookItem(
                                            book.title,
                                            { initReadFragment(book) },
                                            { initAddFragment(book) },
                                        )
                                    }
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                            floatingActionButton = {
                                if (recyclerState.firstVisibleItemIndex <= 0)
                                    FloatingActionButton(
                                        modifier = Modifier.padding(end = 300.dp),
                                        onClick = { initAddFragment(null) },
                                        content = { Icon(imageVector = Icons.Filled.Add, null) },
                                        backgroundColor = MaterialTheme.colors.primary,
                                        contentColor = MaterialTheme.colors.onSecondary,
                                        elevation = FloatingActionButtonDefaults.elevation(
                                            10.dp,
                                            0.dp
                                        )
                                    )
                            },
                        )

                    }
                    // End of composable functions in setScreen
                }
            }
        }
    }


    private fun initAddFragment(book: Book?): ModifyBookDialog = ModifyBookDialog(book).apply {
        show(requireActivity().supportFragmentManager, "Add or Change Book")
    }


    /** Initializes a [FragmentRead] for displaying the book in the reader
     * @param book The book to read
     */
    private fun initReadFragment(book: Book) {
        fragmentTransition(
            requireContext(),
            FragmentRead(book),
            View.GONE
        )
    }

    /**
     *
     *  Composables come after here
     *
     */

    @Composable
    private fun DropdownItem(
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
                } else {
                    MaterialTheme.colors.onPrimary
                }
            )
        }
    }

    @Composable
    private fun ClickableOutlinedText(
        text: String,
        expanded: Boolean,
        modifier: Modifier = Modifier
    ) {
        OutlinedTextField(value = text,
            onValueChange = {},
            enabled = false,
            modifier = modifier,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colors.onPrimary,
                unfocusedBorderColor = MaterialTheme.colors.primary
            ),
            label = {
                Text("Book Lists", color = MaterialTheme.colors.onPrimary)
            },
            trailingIcon = {
                Icon(
                    imageVector = if (!expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                    null,
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        )
    }

    @SuppressLint("ModifierParameter")
    @ExperimentalMaterialApi
    @Composable
    fun BookItem(
        title: String,
        textClickHandler: () -> Unit,
        buttonClickHandler: () -> Unit,
        iconModifier: Modifier = Modifier
    ) {
        ListItem(
            modifier = Modifier
                .clickable { textClickHandler.invoke() }
                .padding(all = 5.dp)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colors.onPrimary),
                    RectangleShape
                ),
            text = {
                Text(
                    title,
                    fontSize = 20.sp,
                    color = MaterialTheme.colors.onPrimary,
                )
            },
            trailing = {
                Box(
                    modifier = Modifier
                        .size(40.dp, 40.dp)
                        .clickable { buttonClickHandler.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        "Modify the book",
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = iconModifier
                            .align(Alignment.Center)
                    )
                }
            }
        )
    }

    /** This is the only Modify List Variant that should be called
     * @param bookTitle
     * @param changeList
     * @param open
     * @param dismissState
     * */
    @Composable
    fun ModifyListDialog(
        /** Initial Title of Book List */
        bookTitle: String?,
        changeList: (String) -> Unit,
        open: Boolean,
        dismissState: (Boolean) -> Unit
    ) {

        if (!open) return
        if (bookTitle != null)
            _ModifyDialogList(bookTitle, dismissState, changeList)
        else
            _AddDialogList(dismissState, changeList)
    }

    /** This should not be called outside of ModifyListDialog*/
    @Composable
    private fun _ModifyDialogList(
        bookTitle: String,
        dismissState: (Boolean) -> Unit,
        changeList: (String) -> Unit
    ) {
        /** Actual text that gets updated */
        var text by remember { mutableStateOf(bookTitle) }

        AlertDialog(
            onDismissRequest = {
                dismissState(false)
                changeList(bookTitle)
            },
            title = {
                Text(text = "Modify $bookTitle")
            },
            text = {
                TextField(text, { text = it }, label = { Text(text = "List item") })
            },
            buttons = {
                Column(
                    Modifier.padding(horizontal = 5.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Delete List button
                        Button(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .fillMaxWidth(.5f),
                            onClick = {
                                dismissState(false)
                                DB.deleteList(BookList(bookTitle))
                                changeList("Books")
                            }
                        ) {
                            Text("Delete $bookTitle")
                        }
                        // Add List button
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                DB.updateList(text, bookTitle)
                                changeList(text)
                                dismissState(false)
                            }
                        ) {
                            Text("Modify $bookTitle")
                        }
                    }
                    // Cancel Button
                    Button(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        onClick = { dismissState(false); changeList(DB.currentTable) }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    /** This should not be called outside of ModifyListDialog */
    @Composable
    private fun _AddDialogList(
        dismissState: (Boolean) -> Unit,
        changeList: (String) -> Unit
    ) {
        /** Actual text that gets updated */
        var text by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                dismissState(false)
                changeList(DB.currentTable)
            },
            title = {
                Text(text = "Add a List")
            },
            text = {
                TextField(text, { text = it }, label = { Text(text = "List item") })
            },
            buttons = {
                Column(
                    Modifier.padding(horizontal = 5.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Add List button
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                DB.addList(BookList(text))
                                changeList(text)
                                dismissState(false)
                            }
                        ) {
                            Text("Add List")
                        }
                    }
                    // Cancel Button
                    Button(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        onClick = { dismissState(false); changeList(DB.currentTable) }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    @Preview
    @Composable
    private fun ModifyListDialog() {
        val openDialog = remember { mutableStateOf(true) }
        var text by remember { mutableStateOf("") }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                title = {
                    Text(text = "Add / Modify a List")
                },
                text = {
                    Column {
                        TextField(
                            value = text,
                            onValueChange = { text = it }
                        )
                    }
                },
                buttons = {
                    Column(
                        modifier = Modifier.padding(all = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(all = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(.5f),
                                onClick = { openDialog.value = false }
                            ) {
                                Text("Delete a Book")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { openDialog.value = false }
                            ) {
                                Text("Add a Book")
                            }
                        }

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { openDialog.value = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    }


}

