package com.cobaltware.webscraper.screens.mainScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.useCases.MainUseCase
import com.cobaltware.webscraper.general.LiveDropdown
import com.cobaltware.webscraper.general.WebscraperTheme
import kotlinx.coroutines.launch


@Composable
fun BookListDropdown(
    data: LiveData<List<BookList>>,
    selectedItem: String,
    setSelectedItem: (String) -> Unit,
    setModifyListOpen: (Boolean) -> Unit,
    setModifyListText: (String?) -> Unit,
    recyclerState: LazyListState,
) {
    Column {
        LiveDropdown(items = data) { items ->

            var expanded by remember { mutableStateOf(false) }
            var dropDownSize by remember { mutableStateOf(IntSize(0, 0)) }

            Column(Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    ClickableOutlinedText(
                        text = selectedItem,
                        labelText = "Book Lists",
                        expanded = expanded,
                        modifier = Modifier
                            .fillMaxWidth(if (items.indexOf(BookList(selectedItem)) != 0) .85f else 1f)
                            .padding(horizontal = 5.dp)
                            // Workaround to get exact height and width of dropdown at runtime
                            .onSizeChanged { dropDownSize = it }
                            .clickable { expanded = !expanded },
                    )

                    if (items.indexOf(BookList(selectedItem)) != 0)
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
                // Dropdown Menu
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

                        // 'Add a BookList' Item
                        DropdownItem(BookList("Add a BookList"), selectedItem) {
                            coroutine.launch {
                                // This opens the add book list menu
                                setModifyListText(null)
                                setModifyListOpen(true)
                                expanded = !expanded
                            }
                        }
                        // All other items
                        items.forEach {
                            DropdownItem(it, selectedItem) {
                                coroutine.launch {
                                    setSelectedItem.invoke(it.name)
                                    setModifyListText(it.name)
                                    currentTable = it.name
                                    recyclerState.scrollToItem(0)
                                    expanded = !expanded
                                }
                            }
                        }

                    }
                }
                // Dropdown Menu
            }
        }
    }
}

/**Represents a dropdown item in the ui**/
@Composable
fun DropdownItem(
    item: BookList,
    selectedItem: String,
    modifier: Modifier = Modifier,
    click: () -> Unit
) {
    WebscraperTheme {
        DropdownMenuItem(
            onClick = click,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
        ) {
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
}

/**Custom outlined text that can be clickable, rather than just focusing in**/
@Composable
fun ClickableOutlinedText(
    text: String,
    labelText: String,
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
            Text(labelText, color = MaterialTheme.colors.onPrimary)
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

@OptIn(ExperimentalMaterialApi::class)
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
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(5.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                title,
                modifier = Modifier.padding(5.dp),
                fontSize = 20.sp,
                color = MaterialTheme.colors.onPrimary,
            )
            Divider(color = MaterialTheme.colors.onPrimary)
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
/**
 * =============================
 * =============================
 * =============================
 * END OF DROPDOWN RELATED ITEMS
 * =============================
 * =============================
 * =============================
 * **/

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
    dismissState: (Boolean) -> Unit,
    useCase: MainUseCase,
) {

    if (!open) return
    if (bookTitle != null)
        _ModifyDialogList(bookTitle, dismissState, changeList, useCase)
    else
        _AddDialogList(dismissState, changeList, useCase)
}

/** This should not be called outside of ModifyListDialog*/
@SuppressLint("ComposableNaming")
@Composable
private fun _ModifyDialogList(
    bookTitle: String,
    dismissState: (Boolean) -> Unit,
    changeList: (String) -> Unit,
    useCase: MainUseCase
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
                            useCase.deleteList(BookList(bookTitle))
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
                            useCase.updateList(text, bookTitle)
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
@SuppressLint("ComposableNaming")
@Composable
private fun _AddDialogList(
    dismissState: (Boolean) -> Unit,
    changeList: (String) -> Unit,
    useCase: MainUseCase
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
                            useCase.addList(BookList(text))
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
