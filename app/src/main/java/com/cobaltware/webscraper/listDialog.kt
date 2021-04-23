package com.cobaltware.webscraper

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.cobaltware.webscraper.datahandling.DB
import kotlinx.android.synthetic.main.menu_add_list.*
import kotlin.String

class ListDialog(context: Context, private var title : String?) : Dialog(context) {
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
        val modify = title != null
        if (domainUrlInput.text.isEmpty())
            return
        val bookListName = domainUrlInput.text.toString()
        if (!modify){
            DB.createBookList(bookListName)
        }
        else {
            DB.modifyTable(null, bookListName)
        }
        dismiss()
    }
    private fun onDeleteClick(){
        if (title != null) {
            DB.deleteTable(title!!)
        }
        dismiss()
    }
}