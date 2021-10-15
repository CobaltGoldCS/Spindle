package com.cobaltware.webscraper.screens.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.useCases.MainUseCase
import com.cobaltware.webscraper.general.HidingFAB
import com.cobaltware.webscraper.general.LiveDropdown
import com.cobaltware.webscraper.general.LiveRecycler
import com.cobaltware.webscraper.general.WebscraperTheme
import com.cobaltware.webscraper.screens.readScreen.FragmentRead
import com.cobaltware.webscraper.screens.settingsScreen.fragmentTransition
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
                        open = modifyListOpen,
                        dismissState = setModifyListOpen,
                        useCase = mainUseCase
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
                                        text = selectedItem,
                                        labelText = "Book Lists",
                                        expanded = expanded,
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
}

