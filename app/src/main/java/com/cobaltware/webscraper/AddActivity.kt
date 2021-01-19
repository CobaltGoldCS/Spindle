package com.cobaltware.webscraper

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_interface.*


class AddActivity : AppCompatActivity() {
    lateinit var bookList : MutableList<Book>
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // Change Gui
        title = "Add / delete a Book"
        setContentView(R.layout.add_interface)
        
        // Get data passed into the functions
        val title = intent.getStringExtra("title")
        val url = intent.getStringExtra("url")
        bookList = intent.getParcelableExtra<ParcelBookList>("bookList")!!.bookList // Check dataHandlers/ParcelBookList

        // On click listeners
        AddButton.setOnClickListener {
            val modify = title != null && url != null
            addBook(textUrl.text.toString(), textName.text.toString(), modify)
        }
        DelButton.setOnClickListener {
            if (textUrl.text.isNotEmpty() && textName.text.isNotEmpty())
            {
                val id = DB.getId(textUrl.text.toString(), textName.text.toString())
                DB.delete(id)
                bookList.removeIf { it.title == textName.text.toString() }
            }
            // Switch back to other screen
            this.updateBookListAndClose()
        }
        // If data is inputted, put the data in the correct text areas
        if (title != null && url != null)
            textName.setText(title, TextView.BufferType.EDITABLE)
            textUrl.setText (  url, TextView.BufferType.EDITABLE)
    }
    private fun addBook(urlInput: String, titleInput: String, modify: Boolean)
    {
        // Needed references to EditText input
        if (urlInput == "" || titleInput == "")
        {
            setResult(0, intent)
            this.finish()
            return
        }
        if (!modify)
        {   // Write new line to database
            if (!DB.checkDuplicate(urlInput)) {
                val insert: Boolean = DB.writeLine(titleInput, urlInput)
                if (!insert)
                    throw IndexOutOfBoundsException("Error occurred when adding new data!")
                val id = DB.getId(urlInput, titleInput)
                bookList.add(Book(id, titleInput, urlInput))
            }
        }
        else
        {   // Modify database
            val url = intent.getStringExtra("url")
            val id = DB.getId(url, titleInput)
            DB.modify(id, urlInput, titleInput)
            bookList.removeIf { it.col_id == id }
            bookList.add(Book(id, titleInput, urlInput))
        }
        // Update values and add them as new line to recycler
        this.updateBookListAndClose()
    }
    private fun updateBookListAndClose()
    {   // This is to make sure that it triggers onActivityResult in MainActivity
        intent.putExtra("newList", ParcelBookList(bookList))
        setResult(1, intent)
        this.finish()
    }
}
