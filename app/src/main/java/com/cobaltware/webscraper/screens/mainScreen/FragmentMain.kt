package com.cobaltware.webscraper.screens.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.BookList
import com.cobaltware.webscraper.datahandling.useCases.MainUseCase
import com.cobaltware.webscraper.general.*
import com.cobaltware.webscraper.screens.readScreen.FragmentRead


class FragmentMain : Fragment() {

    private val mainUseCase by lazy { MainUseCase(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val preference = PreferenceManager.getDefaultSharedPreferences(requireContext())
            WebscraperTheme {
                when (preference.getString("list_screen_values", "dropdown")!!) {
                    Routes.DropdownRoute.route -> {
                        DropdownScreen()
                    }
                    Routes.ListRoute.route -> {
                        ListsScreen(view = this)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun DropdownScreen() = WebscraperTheme {
        var (selectedItem, setSelectedItem) = remember { mutableStateOf(currentTable) }
        // Modify list open items
        val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }
        var (modifyListText, setModifyListText) = remember {
            mutableStateOf<String?>(selectedItem)
        }
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
        val buttonVisibility by remember {
            derivedStateOf {
                recyclerState.firstVisibleItemScrollOffset <= 3
            }
        }
        // This is the main content
        Scaffold(
            topBar = {
                BookListDropdown(
                    data = mainUseCase.readAllLists(),
                    selectedItem = selectedItem,
                    setSelectedItem = setSelectedItem,
                    setModifyListOpen = setModifyListOpen,
                    setModifyListText = setModifyListText,
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
                val animations = AnimationContainer(
                    slideInHorizontally(),
                    slideOutHorizontally(
                        animationSpec = TweenSpec(200, easing = FastOutSlowInEasing),
                    )
                )
                HidingFAB(
                    visibility = buttonVisibility,
                    modifier = Modifier.padding(end = 300.dp), animations) {
                    initAddFragment(null)
                }
            },
        )
    }


    @Composable
    private fun ListsScreen(view: ComposeView) = WebscraperTheme {
        val (modifyListOpen, setModifyListOpen) = remember { mutableStateOf(false) }
        var (modifyListText, setModifyListText) = remember {
            mutableStateOf<String?>("No item selected")
        }
        ModifyListDialog(
            modifyListText,
            changeList = {
                currentTable = it
                modifyListText = it
            },
            open = modifyListOpen,
            dismissState = setModifyListOpen,
            useCase = mainUseCase
        )
        ListScreen("Current Available Lists",
            content = {
                LiveRecycler(mainUseCase.readAllLists()) { lists: List<BookList> ->
                    items(lists) { list ->
                        ListScreenItem(
                            text = list.name,
                            click = { view.setContent { BookScreen(list.name, view) } }
                        ) {
                            if (lists.indexOf(list) != 0)
                                IconButton(onClick = {
                                    setModifyListText(list.name)
                                    setModifyListOpen(true)
                                }) {
                                    Icon(Icons.Filled.Menu, "modify the item")
                                }
                        }
                    }
                }
            },
            action = {
                IconButton(onClick = {
                    setModifyListText(null)
                    setModifyListOpen(true)
                }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add a List")
                }
            }
        )
    }

    @Composable
    fun BookScreen(
        bookList: String,
        view: ComposeView,
    ) {
        ListScreen(title = bookList,
            content = {
                LiveRecycler(
                    mainUseCase.readAllFromBookList(bookList),
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
            action = {
                IconButton(onClick = {
                    initAddFragment(null)
                }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add a List")
                }
            },
            navigation = {
                view.setContent { ListsScreen(view = view) }
            }
        )
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
