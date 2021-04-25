package com.cobaltware.webscraper.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.TextView
import com.cobaltware.webscraper.BookAdapter
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.DB
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.menu_add_book.*
import kotlinx.android.synthetic.main.menu_add_book.view.*
import kotlin.concurrent.thread


class FragmentAdd : BottomSheetDialogFragment() {
    lateinit var bookAdapter : BookAdapter
    lateinit var bookList: MutableList<Book>
    // Supposed to represent if you clicked on an existing book or not
    private var book : Book? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.menu_add_book, container, true)

        val   url = arguments!!.getString("url")
        val title = arguments!!.getString("title")

        thread {
            if (title != null && url != null){
                val id = DB.getIdFromBooklistItem(null, url, title)
                book = Book(id, title, url)
            }
        }


        // Get args for Initialization
        initializeWithView(view, url, title)
        return view
    }

    /**
     * Initializes ui components with view defined in onCreateView
     * @param v The view to reference when changing ui components
     * @param url The Url (If there is one) to populate textUrl with
     * @param title The Title (If there is one) to populate textName with
     */
    private fun initializeWithView(v : View, url : String?, title : String?)
    { thread {
        // If data is inputted, put the data in the correct text areas
        if (title != null && url != null) {
            v.textName.setText(title, TextView.BufferType.EDITABLE)
            v.textUrl.setText(url, TextView.BufferType.EDITABLE)
        }
            // On click listeners
            v.AddButton.setOnClickListener {
                val modify = book != null
                addBook(v.textUrl.text.toString(), v.textName.text.toString(), modify)
            }
            v.DelButton.setOnClickListener {
                if (book != null) {
                    DB.deleteUsingID(null, book!!.col_id)
                    bookList.removeIf { it.col_id == book!!.col_id }
                }
                // Switch back to other screen
                dismiss()
            }
            v.title.setNavigationOnClickListener {
                fragmentTransition(requireActivity() as MainActivity, FragmentMain(), View.VISIBLE)
            }
        }
    }

    private fun addBook(urlInput: String, titleInput: String, modify: Boolean)
    {
        // Needed references to EditText input
        if (!guaranteeValidInputs())
            return
        if (!modify)
        {   // Write new line to database
            if (!DB.checkDuplicateBooklist(null, urlInput)) {

                val insert: Boolean = DB.insertItemIntoTable(null, arrayOf("NAME", "URL").zip(arrayOf(titleInput, urlInput)).toMap())
                if (!insert)
                    throw IndexOutOfBoundsException("Error occurred when adding new data!")
                // Add to recyclerView list
                val id = DB.getIdFromBooklistItem(null, urlInput, titleInput)
                bookList.add(Book(id, titleInput, urlInput))
            }
        }
        else
        {   // Modify database

            DB.modifyItem(null, book!!.col_id, arrayOf("URL", "NAME").zip(arrayOf(urlInput, titleInput)).toMap())
            bookList.removeIf { it.col_id == book!!.col_id }
            bookList.add(Book(book!!.col_id, titleInput, urlInput))
        }
        // Update values and add them as new line to recycler
        this.dismiss()
    }
    private fun guaranteeValidInputs () : Boolean
    {
        if (textName.text.toString().replace("\n", "").isEmpty())
        {
            textName.error = "You have an invalid input"
            return false
        }
        if (!URLUtil.isValidUrl(textUrl.text.toString()))
        {
            textUrl.error = "You have an invalid Url"
            return false
        }
        return true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bookAdapter.changeItems(bookList)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param bookAdapter the adapter used in the recycler that holds the books
         * @param url url to update item to.
         * @param title title to update item to.
         * @return A new instance of fragment AddFragment.
         */
        @JvmStatic
        fun newInstance(bookAdapter: BookAdapter, url : String?, title : String?) =
            FragmentAdd().apply {
                bookList = bookAdapter.bookList.toMutableList()
                this.bookAdapter = bookAdapter
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                }
            }
    }
}