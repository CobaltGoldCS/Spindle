package com.cobaltware.webscraper.screens.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.useCases.MainUseCase
import com.cobaltware.webscraper.general.HidingFAB
import com.cobaltware.webscraper.general.LiveRecycler
import com.cobaltware.webscraper.general.WebscraperTheme
import com.cobaltware.webscraper.general.fragmentTransition
import com.cobaltware.webscraper.screens.readScreen.FragmentRead


class FragmentMain : Fragment() {

    private val mainUseCase by lazy { MainUseCase(requireContext()) }

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
                    // Dialogs
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
                    val recyclerState = rememberLazyListState()
                    // This is the main content
                    Scaffold(
                        topBar = {
                            BookListDropdown(
                                data = mainUseCase.readAllLists(),
                                selectedItem = selectedItem,
                                setSelectedItem = { item -> selectedItem = item },
                                setModifyListOpen = setModifyListOpen,
                                setModifyListText = { item -> modifyListText = item },
                                recyclerState = recyclerState,
                            )
                        },
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

