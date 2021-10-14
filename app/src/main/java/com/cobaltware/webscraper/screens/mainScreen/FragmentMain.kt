package com.cobaltware.webscraper.screens.mainScreen

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.useCases.MainUseCase
import com.cobaltware.webscraper.screens.readScreen.FragmentRead
import com.cobaltware.webscraper.screens.settingsScreen.fragmentTransition
import com.cobaltware.webscraper.general.*
import kotlinx.coroutines.launch


class FragmentMain : Fragment() {

    private val mainUseCase by lazy { MainUseCase(requireContext()) }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var selectedItem by remember { mutableStateOf(currentTable) }
                // Modify list open items
                val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }
                var modifyListText by remember { mutableStateOf<String?>(selectedItem) }


                WebscraperTheme {
                    ModifyListDialog(
                        modifyListText,
                        changeList = {
                            selectedItem = it
                            currentTable = it
                            modifyListText = selectedItem
                        },
                        open = modifyListOpen, dismissState = setModifyListOpen
                    )
                    Column {
                        val recyclerState = rememberLazyListState()
                        LiveDropdown(items = mainUseCase.readAllLists()) { items ->

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
                                            .padding(horizontal = 5.dp)
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

                                Box(
                                    Modifier
                                        .padding(horizontal = 5.dp)
                                        .fillMaxWidth(), contentAlignment = Alignment.Center
                                ) {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = !expanded },
                                        modifier = Modifier
                                            .width(with(LocalDensity.current) { dropDownSize.width.toDp() }),
                                    ) {
                                        val coroutine = rememberCoroutineScope()
                                        items.forEach {
                                            DropdownMenuItem(
                                                onClick = {
                                                    // Handle On Click when dropdown item is pressed
                                                    coroutine.launch {
                                                        if (items.indexOf(it) == 0) {
                                                            // This opens the add book list menu
                                                            modifyListText = null
                                                            setModifyListOpen(true)
                                                        } else {
                                                            selectedItem = it.name
                                                            modifyListText = selectedItem
                                                            currentTable = selectedItem
                                                            recyclerState.scrollToItem(0)
                                                        }
                                                        expanded = !expanded
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 5.dp)
                                            ) {
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
                                    mainUseCase.readAllFromBookList(selectedItem),
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
                            floatingActionButtonPosition = FabPosition.Center,
                            floatingActionButton = {
                                HidingFAB(
                                    visibility = recyclerState.firstVisibleItemIndex <= 0,
                                    modifier = Modifier.padding(end = 300.dp),
                                    onClick = { initAddFragment(null) })
                            },
                        )

                    }
                    // End of composable functions in setScreen
                }
            }
        }
    }


    private fun initAddFragment(book: Book?): ModifyBookDialog {
        val dialog = ModifyBookDialog(book)
        dialog.show(requireActivity().supportFragmentManager, "Add or Change Book")
        return dialog
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

    @ExperimentalMaterialApi
    @Composable
    fun BookItem(
        title: String,
        textClickHandler: () -> Unit,
        buttonClickHandler: () -> Unit,
    ) {

        Card(
            modifier = Modifier
                .padding(5.dp)
                .clickable { textClickHandler.invoke() },
            elevation = if (!isSystemInDarkTheme()) 10.dp else 2.dp,
            shape = RectangleShape,
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    title,
                    modifier = Modifier.padding(5.dp),
                    fontSize = 20.sp,
                    color = MaterialTheme.colors.onPrimary,
                )
                Divider(color = MaterialTheme.colors.onPrimary.copy(1f, 0.7f, 0.7f, 0.7f))
                Button(
                    modifier = Modifier
                        .size(60.dp, 40.dp)
                        .align(Alignment.End)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    onClick = { buttonClickHandler.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        "Modify the book",
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.fillMaxSize(0.9f)
                    )
                }
            }
        }
    }

    /** This is the only Modify List Variant that should be called
     * @param bookTitle The initial name of the book List
     * @param changeList Change the current book list to be displayed
     * @param open If the dropdown menu is open or not
     * @param dismissState A lambda containing the ability to switch [open] from true to false
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
                                mainUseCase.deleteList(BookList(bookTitle))
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
                                mainUseCase.updateList(text, bookTitle)
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
                        onClick = { dismissState(false); changeList(currentTable) }
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
                changeList(currentTable)
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
                                mainUseCase.addList(BookList(text))
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
                        onClick = { dismissState(false); changeList(currentTable) }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }


}

