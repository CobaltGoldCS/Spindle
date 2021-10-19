package com.cobaltware.webscraper

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cobaltware.webscraper.databinding.ActivityMainBinding
import com.cobaltware.webscraper.general.fragmentTransition
import com.cobaltware.webscraper.screens.configScreen.FragmentConfig
import com.cobaltware.webscraper.screens.mainScreen.FragmentMain
import com.cobaltware.webscraper.screens.readScreen.FragmentRead
import com.cobaltware.webscraper.screens.settingsScreen.FragmentSettings


class MainActivity : AppCompatActivity() {
    val view by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        setContentView(view.root)
        setNavTransitions()
        activityFragmentSwitch(FragmentMain())
    }

    override fun onBackPressed() {
        // Override back button behavior in Fragment Read to get to FragmentMain
        if (supportFragmentManager.findFragmentById(R.id.fragmentSpot) is FragmentRead)
            fragmentTransition(this, FragmentMain(), View.VISIBLE)
        else
            super.onBackPressed()
    }

    /** Just for setting up the navigation itemSelectionListener */
    private fun setNavTransitions() {
        view.nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.ic_book -> activityFragmentSwitch(FragmentMain())
                R.id.ic_config -> activityFragmentSwitch(FragmentConfig())
                R.id.ic_settings -> activityFragmentSwitch(FragmentSettings())
            }
            true
        }
    }


    /**Switches to another [fragment] with an animation
     * @param fragment The fragment to switch to*/
    private fun activityFragmentSwitch(fragment: Fragment) {

        val currentFragment =
            supportFragmentManager.findFragmentByTag("Clicked Fragment")?.let { it::class }
        if (currentFragment == fragment::class)
            return
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, fragment, "Clicked Fragment")
        fragmentTrans.commit()
    }
}
