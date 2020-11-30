package com.cobaltware.webscraper

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.reader_view.*

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class ReadActivity : AppCompatActivity() {
    lateinit var db : DataBaseHandler
    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        db = DataBaseHandler(applicationContext)
        setContentView(R.layout.reader_view)
        val values = intent.extras!!
        val url = values["url"].toString()
        val colId = values["col_id"].toString().toInt()
        readBook(colId, url)

        actionBar?.hide()

    }
    private fun readBook(col_id : Int, url : String) : Boolean{
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")
        val currentReader = webpack.callAttr("UrlReading", url)

        val content = currentReader["content"].toString()
        val next    = currentReader["next"   ].toString()
        val prev    = currentReader["prev"   ].toString()
        val title   = currentReader["title"  ].toString()
        contentView.text = content
        setTitle(title)

        db.modify(col_id, url, null)

        nextButton.setOnClickListener { readBook(col_id, next) }
        prevButton.setOnClickListener { readBook(col_id, prev) }
        // Disable and enable depending on the next url
        prevButton.isClickable = prev != "null"
        nextButton.isClickable = next != "null"
        
        arrayOf(prevButton, nextButton).forEach { button ->
            if (!button.isClickable) {
                button.setBackgroundColor(Color.BLACK)
            } else {
                button.setBackgroundColor(Color.parseColor("#09AA37"))
            }
        }

        // TODO: Change background color of button when there is no url
        contentScroll.scrollTo(0,0)
        return true
    }
    fun backButton(v : View){
        setResult(0, intent);
        finish();
    }
}