package com.cobaltware.webscraper

import android.os.Bundle
import android.preference.Preference
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.cobaltware.webscraper.datahandling.DB
import com.cobaltware.webscraper.datahandling.DataBaseHandler
import com.cobaltware.webscraper.fragments.FragmentConfig
import com.cobaltware.webscraper.fragments.FragmentMain
import com.cobaltware.webscraper.fragments.FragmentSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    lateinit var mainFrag: FragmentMain // Referenced by FragmentRead.kt

    private val configFrag = FragmentConfig()
    private val settingsFrag = FragmentSettings()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and setup various background processes
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        DB = DataBaseHandler(this) // This is the ONLY Time the database should be initialized
        thread { mainFrag = FragmentMain() }
        setContentView(R.layout.activity_main)
        setNavTransitions()

        activityFragmentSwitch(mainFrag)

    }

    /** Just for setting up the [nav] itemSelectionListener */
    private fun setNavTransitions() {
        nav.setOnNavigationItemSelectedListener {
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
