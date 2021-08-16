package com.cobaltware.webscraper.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.BookList
import kotlinx.android.synthetic.main.menu_add_list.*

enum class Operations {
    Delete,
    Update,
    Insert,
    Nothing
}

class ModifyListDialog(
    context: Context,
    var title: String?
) : Dialog(context) {
    /** Operation used to determine what the [ModifyListDialog] did */
    var op: Operations = Operations.Nothing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_add_list)
        if (title != null)
            domainUrlInput.setText(title, TextView.BufferType.EDITABLE)
        deleteButton.setOnClickListener { this.onDeleteClick() }
        actionButton.setOnClickListener { this.onActionClick() }
        cancelButton.setOnClickListener { dismiss() }
    }

    /** Handles when the action button is clicked by either modifying or adding a book list **/
    private fun onActionClick() {

        if (domainUrlInput.text.isEmpty()) return
        val bookListName = domainUrlInput.text.toString()


        op = when (title != null) { // Checks if list needs to be updated
            true -> {
                DB.updateList(bookListName, title!!)
                Operations.Update
            }
            false -> {
                DB.addList(BookList(bookListName))
                Operations.Insert
            }
        }
        title = bookListName
        dismiss()
    }

    /**Deletes a book list if it exists**/
    private fun onDeleteClick() {
        if (title != null) {
            DB.deleteList(DB.readList(title!!))
            op = Operations.Delete
        }
        dismiss()
    }
}