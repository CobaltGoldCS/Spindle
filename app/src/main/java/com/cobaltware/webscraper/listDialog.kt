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
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.add_list_menu)
        if (title != null)
            readingListInput.setText(title!!, TextView.BufferType.EDITABLE)
        cancelButton.setOnClickListener {this.onCancelClick()}
        actionButton.setOnClickListener {this.onActionClick()}
    }
    private fun onActionClick(){
        val modify = title != null
        if (readingListInput.text.isEmpty())
            return
        val bookListName = readingListInput.text.toString()
        if (!modify){
            DB.createBookList(bookListName)
        }
        else {
            DB.modifyBookList(bookListName)
        }
        dismiss()
    }
    private fun onCancelClick(){
        if (title != null) {
            DB.deleteBookList(title!!)
        }
        dismiss()
    }
}