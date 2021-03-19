package com.cobaltware.webscraper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.add_interface.*
import kotlinx.android.synthetic.main.add_interface.view.*


class AddFragment : Fragment() {
    lateinit var bookList: MutableList<Book>
    
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.add_interface, container, false)

        // Get args for Initalization
        val url = arguments!!.getString("url")
        val title = arguments!!.getString("title")
        initializeWithView(view, url, title)
        return view
    }

    private fun initializeWithView(v : View, url : String?, title : String?)
    {
        // On click listeners
        v.AddButton.setOnClickListener {
            val modify = title != null && url != null
            addBook(v.textUrl.text.toString(), v.textName.text.toString(), modify)
        }
        v.DelButton.setOnClickListener {
            if (textUrl.text.isNotEmpty() && textName.text.isNotEmpty())
            {
                val id = DB.getId(null, v.textUrl.text.toString(), v.textName.text.toString())
                DB.deleteUsingID(null, id)
                bookList.removeIf { it.title == v.textName.text.toString() }
            }
            // Switch back to other screen
            updateBookListAndClose(bookList)
        }
        // If data is inputted, put the data in the correct text areas
        if (title != null && url != null)
            v.textName.setText(title, TextView.BufferType.EDITABLE)
        v.textUrl.setText (  url, TextView.BufferType.EDITABLE)
    }

    private fun addBook(urlInput: String, titleInput: String, modify: Boolean)
    {
        // Needed references to EditText input
        if (urlInput == "" || titleInput == "")
        {
            updateBookListAndClose(null)
        }
        if (!modify)
        {   // Write new line to database
            if (!DB.checkDuplicateBooklist(null, urlInput)) {
                val insert: Boolean = DB.insertItemIntoBooklist(null, titleInput, urlInput)
                if (!insert)
                    throw IndexOutOfBoundsException("Error occurred when adding new data!")
                // Add to recyclerView list
                val id = DB.getId(null, urlInput, titleInput)
                bookList.add(Book(id, titleInput, urlInput))
            }
        }
        else
        {   // Modify database
            val url = requireActivity().intent.getStringExtra("url")
            val id = DB.getId(null, url, titleInput)

            DB.modifyBooklistItem(null, id, urlInput, titleInput)

            bookList.removeIf { it.col_id == id }
            bookList.add(Book(id, titleInput, urlInput))
        }
        // Update values and add them as new line to recycler
        this.updateBookListAndClose(bookList)
    }
    private fun updateBookListAndClose(bookList : MutableList<Book>?)
    {   // This is to make sure that it triggers onActivityResult in MainActivity
        val fragmentTrans = activity?.supportFragmentManager?.beginTransaction()!!
        fragmentTrans.replace(R.id.fragmentSpot, MainFragment.newInstance(bookList)!!)
        fragmentTrans.commit()
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
            AddFragment().apply {
                bookList = booklist.toMutableList()
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                }
            }
    }
}