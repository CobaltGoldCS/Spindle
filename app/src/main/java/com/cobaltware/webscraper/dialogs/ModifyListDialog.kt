package com.cobaltware.webscraper.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.datahandling.DB
import kotlinx.android.synthetic.main.menu_add_list.*
import kotlin.String

class ModifyListDialog(context: Context, private var title : String?) : Dialog(context) {
    var deleted = false
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_add_list)
        if (title != null)
            domainUrlInput.setText(title!!, TextView.BufferType.EDITABLE)
        deleteButton.setOnClickListener {this.onDeleteClick()}
        actionButton.setOnClickListener {this.onActionClick()}
        cancelButton.setOnClickListener {dismiss()}
    }
    private fun onActionClick(){

        if (domainUrlInput.text.isEmpty()) return
        val bookListName = domainUrlInput.text.toString()

        val modify = title != null
        if (modify){ DB.modifyTableName(null, bookListName) }
        else       { DB.createBookList(bookListName) }
        dismiss()
    }
    private fun onDeleteClick(){
        if (title != null) {
            DB.deleteTable(title!!)
            deleted = true
        }
        dismiss()
    }
}