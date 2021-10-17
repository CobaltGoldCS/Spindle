package com.cobaltware.webscraper

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cobaltware.webscraper.screens.configScreen.FragmentConfig
import com.cobaltware.webscraper.screens.mainScreen.FragmentMain
import com.cobaltware.webscraper.screens.settingsScreen.FragmentSettings
import com.cobaltware.webscraper.databinding.ActivityMainBinding
import com.cobaltware.webscraper.screens.readScreen.FragmentRead
import com.cobaltware.webscraper.general.fragmentTransition


class MainActivity : AppCompatActivity() {
    val view by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val main = FragmentMain()
    private val config = FragmentConfig()
    private val settings = FragmentSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        setContentView(view.root)
        setNavTransitions()
        activityFragmentSwitch(main)
    }

    override fun onBackPressed() {
        // Override back button behavior in Fragment Read to get to FragmentMain
        if (supportFragmentManager.findFragmentById(R.id.fragmentSpot) is FragmentRead)
            fragmentTransition(this, main, View.VISIBLE)
        else
            super.onBackPressed()
    }

    /** Just for setting up the navigation itemSelectionListener */
    private fun setNavTransitions() {
        view.nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.ic_book -> activityFragmentSwitch(main)
                R.id.ic_config -> activityFragmentSwitch(config)
                R.id.ic_settings -> activityFragmentSwitch(settings)
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
