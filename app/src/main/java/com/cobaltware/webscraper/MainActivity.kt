package com.cobaltware.webscraper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cobaltware.webscraper.datahandling.BookViewModel
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.fragments.FragmentConfig
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.FragmentSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    lateinit var mainFrag: FragmentMain  // Referenced by FragmentRead.kt
    private lateinit var configFrag: FragmentConfig
    private val settingsFrag = FragmentSettings()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        thread {
            configFrag = FragmentConfig()
        }
        setContentView(R.layout.activity_main)
        setNavTransitions()


        mainFrag = FragmentMain()
        activityFragmentSwitch(mainFrag)
    }

    /** Just for setting up the [nav] itemSelectionListener */
    private fun setNavTransitions() {
        nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.ic_book -> activityFragmentSwitch(mainFrag)
                R.id.ic_config -> activityFragmentSwitch(configFrag)
                R.id.ic_settings -> activityFragmentSwitch(settingsFrag)
            }
            true
        }
    }


    /**Switches to another [fragment] with an animation
     * @param fragment The fragment to switch to*/
    private fun activityFragmentSwitch(fragment: Fragment) {
        if (fragment.isVisible)
            return // Prevent reloading when using the fragment again
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, fragment)
        fragmentTrans.commit()
    }
}
