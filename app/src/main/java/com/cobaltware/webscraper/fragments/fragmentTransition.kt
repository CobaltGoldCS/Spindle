package com.cobaltware.webscraper.fragments

import android.app.Activity
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.google.android.material.bottomnavigation.BottomNavigationView

fun fragmentTransition(activity : MainActivity, targetFragment : Fragment, visibility : Int)
{
    val fragmentTrans = activity.supportFragmentManager.beginTransaction()
    fragmentTrans.setCustomAnimations(R.animator.frag_fade_in, R.animator.frag_fade_out)
    fragmentTrans.replace(R.id.fragmentSpot, targetFragment)
    fragmentTrans.commit()
    activity.requireViewById<BottomNavigationView>(R.id.nav).visibility = visibility
}