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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
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
import kotlinx.coroutines.launch
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
                        val recyclerState = rememberLazyListState()
                        LiveDropdown(items = DB.readAllLists){ items ->

                            var expanded by remember { mutableStateOf(false) }
                            var dropDownSize by remember { mutableStateOf(IntSize(0, 0)) }

                            Column(Modifier,  horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val onPrimary = MaterialTheme.colors.onPrimary

                                    OutlinedTextField(value = selectedItem.toString(),
                                        onValueChange = {},
                                        enabled = false,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            disabledTextColor = onPrimary,
                                            unfocusedBorderColor =  MaterialTheme.colors.primary),
                                        modifier = Modifier
                                            .fillMaxWidth(if (items.indexOf(BookList(selectedItem)) > 1) .85f else 1f)
                                            .padding(start = 5.dp, end = 5.dp)
                                            // Workaround to get exact height and width of dropdown at runtime
                                            .onSizeChanged { dropDownSize = it }
                                            .clickable { expanded = !expanded },
                                        label = {
                                            Text("Book Lists", color = onPrimary)
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = if(!expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                                                null,
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                tint = onPrimary
                                            )
                                        }
                                    )
                                    if (items.indexOf(BookList(selectedItem)) > 1)
                                        OutlinedButton(onClick = { setModifyListOpen(true) }, modifier = Modifier
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
                                        onDismissRequest = {expanded = !expanded},
                                        modifier = Modifier
                                            .width(with(LocalDensity.current){dropDownSize.width.toDp()})
                                    ) {
                                        val coroutine = rememberCoroutineScope()
                                        items.forEach {
                                            DropdownMenuItem(onClick = {
                                                // Handle On Click when dropdown item is pressed
                                                coroutine.launch {
                                                    if (items.indexOf(it) == 0) {
                                                        modifyListText = null
                                                        setModifyListOpen(true)
                                                    }
                                                    else {
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
                                LiveRecycler(DB.readAllFromBookList(selectedItem), recyclerState) { list: List<Book> ->
                                    items(list) { book ->
                                        viewController.BookItem(
                                            book.title,
                                            { viewController.initReadFragment(book) },
                                            { viewController.initAddFragment(book) },
                                        )
                                    }
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                            floatingActionButton = {
                                if (!(recyclerState.firstVisibleItemIndex > 0))
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
