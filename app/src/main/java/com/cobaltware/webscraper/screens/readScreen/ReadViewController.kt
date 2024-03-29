package com.cobaltware.webscraper.screens.readScreen

import android.content.SharedPreferences
import android.graphics.Color
import android.webkit.WebView
import androidx.core.view.isVisible
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.databinding.FragmentReadBinding

class ReadViewController(val view: FragmentReadBinding) {


    public val browser: WebView = view.browser

    /** Handles updates to the ui that have to do with preferences
     * @param preferences The preferences reference to base the ui off of
     * @param themeTextColor The color used by the theme for text in the reader
     * */
    fun setPreferenceUI(preferences: SharedPreferences, themeTextColor: Int) {
        view.contentView.textSize = preferences.getString("textsize", "22")!!.toFloat()

        view.contentView.setTextColor(
            if (preferences.getBoolean("highcontrast", true)) {
                themeTextColor
            } else {
                if (themeTextColor == Color.WHITE) Color.LTGRAY else Color.GRAY
            }
        )

    }


    /** Updates UI using article variables
     *  @param title Title of the chapter
     *  @param content Content of the chapter
     *  @param prevUrl The url to the previous chapter
     *  @param nextUrl The url to the next chapter*/
    fun updateUi(
        title: String,
        content: String,
        prevUrl: String?,
        nextUrl: String?
    ) {
        view.scrollTitle.title = title
        view.contentView.text = content

        // Hide buttons when they cannot be used
        view.prevButton.isVisible = (prevUrl != null) && (prevUrl != "null")
        view.nextButton.isVisible = (nextUrl != null) && (nextUrl != "null")

        // Title modifications to fit
        when {
            title.length > 60 -> view.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium)
            title.length > 20 -> view.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Large)
            else -> view.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded)
        }

        view.contentScroll.scrollTo(0, 0)
    }
}