package com.cobaltware.webscraper.dialogs


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
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.MenuAddBookBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.fragmentTransition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.concurrent.thread


class ModifyBookDialog(private var book: Book? = null) : BottomSheetDialogFragment() {


    var op: Operations = Operations.Nothing
    var position: Int = 0
    private var clickListener: () -> Unit = { }

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
        Log.d("Current Table should be ", DB.currentTable)
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
                    DB.deleteBook(book!!)
                    op = Operations.Delete
                }
                dismiss()
            }
            v.title.setNavigationOnClickListener {
                fragmentTransition(
                    FragmentMain(),
                    View.VISIBLE
                )
            }
            v.bookLists.setOnItemClickListener { _, _, position, _ ->
                DB.currentTable = adapter.getItem(position)!!
                this.position = position
            }
            requireActivity().runOnUiThread {
                DB.readAllLists().observe(viewLifecycleOwner) { booklists ->
                    booklists.forEach {
                        if (booklists.indexOf(it) > 0)
                            adapter.add(it.name)
                    }
                    adapter.notifyDataSetChanged()
                }
                v.bookLists.setAdapter(adapter)
                // Set item in dropdown
                v.bookLists.setText(DB.currentTable, false)
                v.bookLists.listSelection = adapter.getPosition(DB.currentTable)
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
        if (book == null) {   // Write new line to database
            val newBook = Book(0, titleInput, urlInput, DB.currentTable)
            DB.addBook(newBook)
            op = Operations.Insert
            // Add to recyclerView list
        } else {   // Modify database
            DB.updateBook(
                Book(book!!.row_id, titleInput, urlInput, DB.currentTable)
            )
            op = Operations.Update
        }
        this.dismiss()
    }


    /** Makes sure that all inputs are valid; otherwise gives certain errors to the textviews
     * @return if both name and url's inputs are valid
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

    fun addDismissListener(lambda: () -> Unit) {
        clickListener = lambda
    }
}