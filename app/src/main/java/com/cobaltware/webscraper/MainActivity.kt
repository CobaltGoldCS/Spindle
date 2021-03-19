package com.cobaltware.webscraper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        /* Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        DB = DataBaseHandler(this) // This is the ONLY Time the database should be changed

        initSimpleUiComponents()
        setupRecyclerView()
        setupDropdown()
         */
        setContentView(R.layout.main_bar)

        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, MainFragment.newInstance(null)!!)
        fragmentTrans.commit()

    }
}