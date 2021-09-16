package com.cobaltware.webscraper.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.MenuAddListBinding
import com.cobaltware.webscraper.datahandling.BookList

enum class Operations {
    Delete,
    Update,
    Insert,
    Nothing
}

class ModifyListDialog(
    private val thisContext: Context,
    var title: String?
) : Dialog(thisContext) {
    /** Operation used to determine what the [ModifyListDialog] did */
    var op: Operations = Operations.Nothing
    val view by lazy{ MenuAddListBinding.inflate(LayoutInflater.from(thisContext)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_add_list)
        if (title != null)
            view.textName.setText(title)
        view.deleteButton.setOnClickListener { this.onDeleteClick() }
        view.actionButton.setOnClickListener { this.onActionClick() }
        view.cancelButton.setOnClickListener { dismiss() }
    }

    /** Handles when the action button is clicked by either modifying or adding a book list **/
    private fun onActionClick() {

        if (view.textName.text.isEmpty()) return
        val bookListName = view.textName.text.toString()


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