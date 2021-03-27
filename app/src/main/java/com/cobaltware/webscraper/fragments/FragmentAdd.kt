package com.cobaltware.webscraper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.DB
import kotlinx.android.synthetic.main.fragment_add.view.*


class FragmentAdd : Fragment() {
    lateinit var bookList: MutableList<Book>
    // Supposed to represent if you clicked on an existing book or not
    private var book : Book? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        // Get args for Initialization
        val   url = arguments!!.getString("url")
        val title = arguments!!.getString("title")
        initializeWithView(view, url, title)
        return view
    }

    private fun initializeWithView(v : View, url : String?, title : String?)
    {
        // If data is inputted, put the data in the correct text areas
        if (title != null && url != null){
            v.textName.setText(title, TextView.BufferType.EDITABLE)
            v.textUrl.setText (  url, TextView.BufferType.EDITABLE)

            val id = DB.getIdFromBooklistItem(null, url, title)
            book = Book(id, title, url)
        }

        // On click listeners
        v.AddButton.setOnClickListener {
            val modify = book != null
            addBook(v.textUrl.text.toString(), v.textName.text.toString(), modify)
        }
        v.DelButton.setOnClickListener {
            if (book != null)
            {
                DB.deleteUsingID(null, book!!.col_id)
                bookList.removeIf { it.col_id == book!!.col_id}
            }
            // Switch back to other screen
            dismiss()
        }
    }

    private fun addBook(urlInput: String, titleInput: String, modify: Boolean)
    {
        // Needed references to EditText input
        if (urlInput.replace(" ", "").isEmpty() || titleInput.replace(" ", "").isEmpty())
            dismiss()
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
    private fun dismiss()
    {
        val activity : MainActivity = activity as MainActivity
        fragmentTransition(activity, FragmentMain(), View.VISIBLE)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param booklist list to update.
         * @param url url to update item to.
         * @param title title to update item to.
         * @return A new instance of fragment AddFragment.
         */
        @JvmStatic
        fun newInstance(booklist: List<Book>, url : String?, title : String?) =
            FragmentAdd().apply {
                bookList = booklist.toMutableList()
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                }
            }
    }
}