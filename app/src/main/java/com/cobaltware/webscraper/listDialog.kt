package com.cobaltware.webscraper

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import kotlinx.android.synthetic.main.add_list_menu.*
import kotlin.String

class ListDialog(context: Context, private var title : String?) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_list_menu)
        if (title != null)
            domainUrlInput.setText(title!!, TextView.BufferType.EDITABLE)
        deleteButton.setOnClickListener {this.onCancelClick()}
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
    private fun onCancelClick(){
        if (title != null) {
            DB.deleteTable(title!!)
        }
        dismiss()
    }
}