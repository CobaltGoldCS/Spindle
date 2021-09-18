package com.cobaltware.webscraper.viewcontrollers

import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.FragmentMainBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.dialogs.ModifyListDialog
import com.cobaltware.webscraper.dialogs.Operations
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.FragmentRead
import com.cobaltware.webscraper.fragments.fragmentTransition

class MainViewController(private val fragment: Fragment) {

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

    private fun getColor(res: Int): Color {
        val typedValue = TypedValue()
        val theme = fragment.requireContext().theme
        theme.resolveAttribute(res, typedValue, true)
        return Color(typedValue.data)
    }

    @ExperimentalMaterialApi
    @Composable
    fun BookItem(
        title: String,
        textClickHandler: () -> Unit,
        buttonClickHandler: () -> Unit,
    ) {
        val colorPrimary = getColor(R.attr.colorOnPrimary)
        ListItem(
            modifier = Modifier
                .clickable { textClickHandler.invoke() }
                .padding(all = 5.dp)
                .border(
                    BorderStroke(2.dp, colorPrimary),
                    RectangleShape
                ),
            text = {
                Text(
                    title,
                    fontSize = 20.sp,
                    color = colorPrimary,
                )
            },
            trailing = {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    "Modify the book",
                    tint = colorPrimary,
                    modifier = Modifier.clickable { buttonClickHandler.invoke() }
                )
            }
        )
    }

    @Composable
    fun ModifyListDialog(
        /** Initial Title of Book List */
        title: String?,
        changeList: (String) -> Unit,
        open: Boolean,
        dismissState: (Boolean) -> Unit
    ) {

        if (!open) return
        if (title != null)
            ModifyDialogList(title, dismissState, changeList)
        else
            AddDialogList(dismissState, changeList)
    }

    @Composable
    private fun ModifyDialogList(
        title: String,
        dismissState: (Boolean) -> Unit,
        changeList: (String) -> Unit
    ) {
        /** Actual text that gets updated */
        var text by remember { mutableStateOf(title) }

        AlertDialog(
            onDismissRequest = {
                dismissState(false)
                changeList(title)
            },
            title = {
                Text(text = "Modify $title")
            },
            text = {
                TextField(text, { text = it }, label = { Text(text = "List item")})
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
                                DB.deleteList(BookList(title))
                                changeList("Books")
                            }
                        ) {
                            Text("Delete $title")
                        }
                        // Add List button
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                DB.updateList(text, title)
                                changeList(text)
                                dismissState(false)
                            }
                        ) {
                            Text("Modify $title")
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

    @Composable
    private fun AddDialogList(
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
                TextField(text, { text = it }, label = { Text(text = "List item")})
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