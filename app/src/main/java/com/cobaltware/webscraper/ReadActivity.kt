package com.cobaltware.webscraper

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.reader_view.*

import com.chaquo.python.Python

class ReadActivity : AppCompatActivity() {
    lateinit var db : DataBaseHandler
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        // TODO: Set loading Animation while requesting data
        // Getting important values
        val values = intent.extras!!
        val url = values["url"].toString()
        val colId = values["col_id"].toString().toInt()
        // Set Up Database
        db = DataBaseHandler(applicationContext)
        db.tableName = values["database_TableName"].toString()
        // Customize actionbar
        supportActionBar!!.hide()
        // On android R make built in nav bar at the bottom invisible unless swiped
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
        // Initial UI Setup
        setContentView(R.layout.reader_view)
        scrolltitle.movementMethod = ScrollingMovementMethod()
        readBook(colId, url)
    }
    private fun readBook(col_id : Int, url : String) : String
    {
        // Set up python stuff, and call UrlReading Class with a Url
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")
        val currentReader = webpack.callAttr("UrlReading", url)
        // Get data from UrlReading Instance
        val content = currentReader["content"].toString()
        val next    = currentReader["next"   ].toString()
        val prev    = currentReader["prev"   ].toString()
        val title   = currentReader["title"  ].toString()
        // Update Elements with info
        contentView.text = content
        scrolltitle.text = title

        prevButton.visibility = if (prev != "null") View.VISIBLE else View.GONE
        nextButton.visibility = if (next != "null") View.VISIBLE else View.GONE

        nextButton.setOnClickListener { readBook(col_id, next) }
        prevButton.setOnClickListener { readBook(col_id, prev) }
        // Update database with the new url
        db.modify(col_id, url, null)


        contentScroll.scrollTo(0,0)
        return title
    }
    fun backButton(v : View)
    {
        setResult(0, intent)
        finish()
    }
}