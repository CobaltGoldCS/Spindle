package com.cobaltware.webscraper.fragments

import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import kotlinx.android.synthetic.main.activity_main.*

/** Function for transitioning between fragments so that I don't forget anything on the ui
 * @param activity The Main Activity, because we need to access some of it's methods
 * @param targetFragment The fragment to switch to
 * @param visibility The desired visibility of the nav bar component
 */
fun fragmentTransition(activity: MainActivity, targetFragment: Fragment, visibility: Int) {
    val fragmentTrans = activity.supportFragmentManager.beginTransaction()
    fragmentTrans.setCustomAnimations(R.animator.frag_fade_in, R.animator.frag_fade_out)
    fragmentTrans.replace(R.id.fragmentSpot, targetFragment)
    fragmentTrans.commit()
    activity.runOnUiThread { activity.nav.visibility = visibility }
}