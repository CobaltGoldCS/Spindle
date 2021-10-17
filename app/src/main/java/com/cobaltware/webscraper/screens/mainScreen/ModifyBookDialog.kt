package com.cobaltware.webscraper.screens.mainScreen


import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.TextView
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.currentTable
import com.cobaltware.webscraper.databinding.MenuAddBookBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.useCases.ModifyBookDialogUseCase
import com.cobaltware.webscraper.general.fragmentTransition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.concurrent.thread


class ModifyBookDialog(private var book: Book? = null) : BottomSheetDialogFragment() {

    private val dataHandler: ModifyBookDialogUseCase by lazy {
        ModifyBookDialogUseCase(requireContext())
    }


    private var clickListener: () -> Unit = { }
    private var tempBookList: String = currentTable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = MenuAddBookBinding.inflate(layoutInflater)
        initializeWithView(view)
        return view.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        clickListener.invoke()
        Log.d("Current Table should be ", currentTable)
    }

    /**
     * Initializes ui components with view defined in onCreateView
     * @param v The view to reference when changing ui components
     */
    private fun initializeWithView(v: MenuAddBookBinding) {
        thread {
            // If data is inputted, put the data in the correct text areas
            if (book != null) {
                v.textName.setText(book!!.title, TextView.BufferType.EDITABLE)
                v.textUrl.setText(book!!.url, TextView.BufferType.EDITABLE)
            }
            // Dropdown stuff
            val adapter =
                ArrayAdapter(requireContext(), R.layout.item_dropdown, mutableListOf<String>())

            // On click listeners
            v.AddButton.setOnClickListener {
                addOrModifyBook(v, v.textUrl.text.toString(), v.textName.text.toString())
            }
            v.DelButton.setOnClickListener {
                if (book != null) {
                    dataHandler.deleteBook(book!!)
                }
                dismiss()
            }
            v.title.setNavigationOnClickListener {
                fragmentTransition(
                    requireContext(),
                    FragmentMain(),
                    View.VISIBLE
                )
            }
            v.bookLists.setOnItemClickListener { _, _, position, _ ->
                tempBookList = adapter.getItem(position)!!
            }
            requireActivity().runOnUiThread {
                dataHandler.readAllLists().observe(viewLifecycleOwner) { bookLists ->
                    bookLists.forEach { adapter.add(it.toString()) }
                    adapter.notifyDataSetChanged()
                }
                v.bookLists.setAdapter(adapter)
                // Set item in dropdown
                v.bookLists.setText(currentTable, false)
                v.bookLists.listSelection = adapter.getPosition(currentTable)
                v.bookLists.performClick()
                v.bookLists.performCompletion()
            }
        }
    }

    private fun addOrModifyBook(
        v: MenuAddBookBinding,
        urlInput: String,
        titleInput: String
    ) {
        if (!guaranteeValidInputs(v))
            return
        currentTable = tempBookList
        if (book == null) {   // Write new line to database
            val newBook = Book(0, titleInput, urlInput, currentTable)
            dataHandler.addBook(newBook)
            // Add to recyclerView list
        } else {   // Modify database
            dataHandler.updateBook(
                book!!.copy(title = titleInput, url = urlInput, bookList = currentTable)
            )
        }
        this.dismiss()
    }


    /** Makes sure that all inputs are valid; otherwise gives certain errors to the text views
     * @return if both name and url inputs are valid
     */
    private fun guaranteeValidInputs(view: MenuAddBookBinding): Boolean {
        if (view.textName.text.toString().replace("\n", "").isEmpty()) {
            view.textName.error = "You have an invalid input"
            return false
        }
        if (!URLUtil.isValidUrl(view.textUrl.text.toString())) {
            view.textUrl.error = "You have an invalid Url"
            return false
        }
        return true
    }
}