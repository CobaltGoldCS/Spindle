package com.cobaltware.webscraper.screens.mainScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.useCases.MainUseCase
import com.cobaltware.webscraper.general.WebscraperTheme

/**Represents a dropdown item in the ui**/
@Composable
fun DropdownItem(
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