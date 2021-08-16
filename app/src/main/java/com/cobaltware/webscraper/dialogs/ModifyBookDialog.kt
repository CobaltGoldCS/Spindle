package com.cobaltware.webscraper.dialogs


import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.TextView
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.fragmentTransition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.menu_add_book.*
import kotlinx.android.synthetic.main.menu_add_book.view.*
import kotlin.concurrent.thread


class ModifyBookDialog : BottomSheetDialogFragment() {


    // Supposed to represent if you clicked on an existing book or not
    private var book: Book? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.menu_add_book, container, true)
        initializeWithView(view)
        return view
    }

    /**
     * Initializes ui components with view defined in onCreateView
     * @param v The view to reference when changing ui components
     */
    private fun initializeWithView(v: View) {
        thread {
            // If data is inputted, put the data in the correct text areas
            if (book != null) {
                v.textName.setText(book!!.title, TextView.BufferType.EDITABLE)
                v.textUrl.setText(book!!.url, TextView.BufferType.EDITABLE)
            }
            // On click listeners
            v.AddButton.setOnClickListener {
                addOrModifyBook(v.textUrl.text.toString(), v.textName.text.toString())
            }
            v.DelButton.setOnClickListener {
                if (book != null) {
                    DB.deleteBook(book!!)
                }
                // Switch back to other screen
                dismiss()
            }
            v.title.setNavigationOnClickListener {
                fragmentTransition(
                    requireActivity() as MainActivity,
                    FragmentMain(),
                    View.VISIBLE
                )
            }
        }
    }

    private fun addOrModifyBook(urlInput: String, titleInput: String) {
        if (!guaranteeValidInputs())
            return
        if (book == null) {   // Write new line to database
            val newBook = Book(0, titleInput, urlInput, DB.currentTable)
            DB.addBook(newBook)
            // Add to recyclerView list
        } else {   // Modify database
            DB.updateBook(
                Book(book!!.row_id, titleInput, urlInput, book!!.bookList)
            )
        }
        this.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("Current Table should be ", DB.currentTable)
    }

    /** Makes sure that all inputs are valid; otherwise gives certain errors to the textviews
     * @return if both [textName] and [textUrl]'s inputs are valid
     */
    private fun guaranteeValidInputs(): Boolean {
        if (textName.text.toString().replace("\n", "").isEmpty()) {
            textName.error = "You have an invalid input"
            return false
        }
        if (!URLUtil.isValidUrl(textUrl.text.toString())) {
            textUrl.error = "You have an invalid Url"
            return false
        }
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param book The book to modify
         * @return A new instance of fragment AddFragment.
         */
        @JvmStatic
        fun newInstance(
            book: Book?
        ) =
            ModifyBookDialog().apply {
                this.book = book
            }
    }
}