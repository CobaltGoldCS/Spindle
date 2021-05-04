package com.cobaltware.webscraper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cobaltware.webscraper.datahandling.DB
import com.cobaltware.webscraper.datahandling.DataBaseHandler
import com.cobaltware.webscraper.fragments.FragmentConfig
import com.cobaltware.webscraper.fragments.FragmentMain
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        DB = DataBaseHandler(this) // This is the ONLY Time the database should be initialized
        setContentView(R.layout.activity_main)
        setNavTransitions()

        activityFragmentSwitch(FragmentMain())

    }
    private fun setNavTransitions()
    {
        nav.setOnNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.ic_book -> activityFragmentSwitch(FragmentMain())
                R.id.ic_settings -> activityFragmentSwitch(FragmentConfig())
            }
            true
        }
    }
    private fun activityFragmentSwitch(fragment: Fragment)
    {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, fragment)
        fragmentTrans.commit()
    }
}
