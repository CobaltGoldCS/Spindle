package com.cobaltware.webscraper

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_interface.*


class AddActivity : AppCompatActivity() {
    lateinit var db : DataBaseHandler
    lateinit var bookList : MutableList<Book>
    lateinit var adapter : BookAdapter
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        db = DataBaseHandler(applicationContext)
        title = "Add / Change a Book"
        val title = intent.getStringExtra("title")
        val url = intent.getStringExtra("url")
        // Gets Wrapper that contains bookList, then converts it back to a Mutable list of Books
        bookList = intent.getParcelableExtra<Wrapper>("bookList")!!.bookList

        setContentView(R.layout.add_interface)
        AddButton.setOnClickListener {
            val modify = title != null && url != null
            addBook(textUrl.text.toString(), textName.text.toString(), modify)
        }
        DelButton.setOnClickListener {
            val id = db.getId(textUrl.text.toString(), textName.text.toString())
            db.delete(id)
            bookList.removeIf { it.title == textName.text.toString() }
            // Switch back to other screen
            giveNClose()
        }

        if (title != null && url != null)
            textName.setText(title, TextView.BufferType.EDITABLE)
            textUrl.setText(url, TextView.BufferType.EDITABLE)
        // TODO: On Enter Hide Keyboard
    }
    private fun addBook(urlInput: String, titleInput: String, modify: Boolean){
        // Needed references to EditText input
        if (urlInput == "" || titleInput == "") {
            setResult(0, intent)
            this.finish()
            return
        }
        if (!modify) {
            // Write new line to database
            if (!db.checkDuplicate(urlInput)) {
                val insert: Boolean = db.writeLine(titleInput, urlInput)
                if (!insert)
                    throw IndexOutOfBoundsException("Error occurred when adding new data!")
                val id = db.getId(urlInput, titleInput)
                bookList.add(Book(id, titleInput, urlInput))
            }
        }
        else{ // Modify database
            val url = intent.getStringExtra("url")
            val id = db.getId(url!!, titleInput)
            db.modify(id, urlInput, titleInput)
            bookList.removeIf { it.col_id == id }
            bookList.add(Book(id, titleInput, urlInput))
        }
        // Update values and add them as new line to recycler
        giveNClose()
    }
    private fun giveNClose(){
        // This is to make sure that it triggers onActivityResult in MainActivity
        intent.putExtra("newList", Wrapper(bookList))
        setResult(1, intent)
        this.finish()
    }
}
