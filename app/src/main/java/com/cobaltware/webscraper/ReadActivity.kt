package com.cobaltware.webscraper

import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.chaquo.python.Python
import kotlinx.android.synthetic.main.reader_view.*
import java.util.concurrent.*


class ReadActivity : AppCompatActivity() {

    private var col_id: Int = 0
    private val executor : Executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // Getting important values
        val values = intent.extras!!
        val url = values["url"] as String
        col_id = values["col_id"] as Int

        // Initial UI Setup
        setContentView(R.layout.reader_view)
        scrolltitle.movementMethod = ScrollingMovementMethod()

        // On android R make built in nav bar at the bottom invisible unless swiped
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE

        prevButton.isVisible = false
        nextButton.isVisible = false

        executor.execute { asyncUrlLoad(url) }
    }
    private fun asyncUrlLoad(url: String)
    {
        // Get the data
        val completableFuture: CompletableFuture<List<String>> = CompletableFuture.supplyAsync { getUrlInfo(url) }
        val data : List<String> = completableFuture.get()
        Log.d("data", "Data obtained asyncUrlLoad")
        // Update the gui
        runOnUiThread {
            Log.d("GUI", "Update UI from asyncUrlLoad")
            updateUi(data)
        }
    }
    private fun getUrlInfo(url: String) : List<String>
    {
        // Set up python stuff, and call UrlReading Class with a Url
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")
        val currentReader = webpack.callAttr("UrlReading", url)
        val returnList = mutableListOf<String>()
        // Get data from UrlReading Instance
        returnList.add(currentReader["title"  ].toString())
        returnList.add(currentReader["content"].toString())
        returnList.add(currentReader["prev"   ].toString())
        returnList.add(currentReader["next"   ].toString())
        returnList.add(url)
        // Update Elements with info
        return returnList
    }
    private fun updateUi(list: List<String>){
        scrolltitle.text = list[0]
        contentView.text = list[1]

        DB.modify(col_id, list[4], null)

        prevButton.setOnClickListener {executor.execute{ asyncUrlLoad(list[2]) }}
        nextButton.setOnClickListener {executor.execute{ asyncUrlLoad(list[3]) }}

        prevButton.isVisible = list[2] != "null"
        nextButton.isVisible = list[3] != "null"

        contentScroll.scrollTo(0,0)
    }
}