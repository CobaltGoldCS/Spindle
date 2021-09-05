package com.cobaltware.webscraper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cobaltware.webscraper.fragments.FragmentConfig
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.FragmentSettings
import com.cobaltware.webscraper.ReaderApplication.Companion.activity
import com.cobaltware.webscraper.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val view by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        activity = this
        setContentView(view.root)
        setNavTransitions()
        activityFragmentSwitch(FragmentMain())
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
        if (fragment.isVisible)
            return // Prevent reloading when using the fragment again
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragmentSpot, fragment)
        fragmentTrans.commit()
    }
}
