package com.cobaltware.webscraper.fragments

import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.activity
import com.cobaltware.webscraper.R

/** Function for transitioning between fragments so that I don't forget anything on the ui
 * @param targetFragment The fragment to switch to
 * @param visibility The desired visibility of the nav bar component
 */
fun fragmentTransition(targetFragment: Fragment, visibility: Int) {
    val fragmentTrans = activity.supportFragmentManager.beginTransaction()
    fragmentTrans.setCustomAnimations(R.animator.frag_fade_in, R.animator.frag_fade_out)
    fragmentTrans.replace(R.id.fragmentSpot, targetFragment)
    fragmentTrans.commit()
    activity.runOnUiThread { activity.view.nav.visibility = visibility }
}