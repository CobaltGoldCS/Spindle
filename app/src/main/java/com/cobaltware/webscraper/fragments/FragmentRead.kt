package com.cobaltware.webscraper.fragments

import android.accounts.NetworkErrorException
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.preference.PreferenceManager
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.webhandlers.webdata
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_read.*
import kotlinx.android.synthetic.main.fragment_read.view.*
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread


class FragmentRead() : Fragment() {

    private lateinit var book: Book
    lateinit var viewer: View

    private val nextHandler = PageChangeHandler(this)
    private val prevHandler = PageChangeHandler(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewer = inflater.inflate(R.layout.fragment_read, container, false)
        // Getting important values


        setStaticUI()

        // First time run
        thread {
            vibrate(100)
            Log.d("data", "Obtaining data")
            val data: List<String?> = getUrlInfo(book.url)

            if (data.isNullOrEmpty()) {
                // Error handling
                Log.e("Domain Error", "Domain may not be properly supported; exiting")
                quit("This domain / url is not properly supported in the configs")
                return@thread
            }

            vibrate(150)
            // Update the gui
            try {
                requireActivity().runOnUiThread {
                    Log.d("GUI", "Update UI from asyncUrlLoad")
                    updateUi(data[0]!!, data[1]!!, data[2], data[3], data[4]!!)
                }
            } catch (e: Exception) { /* Here to catch edge cases when activity has been garbage collected */
            }
        }
        return viewer
    }

    /** Sets UI that shouldn't be changed **/
    private fun setStaticUI() {
        viewer.scrollable.setNavigationOnClickListener { quit() }
        viewer.nextButton.setOnClickListener { thread { nextHandler.changePage() } }
        viewer.prevButton.setOnClickListener { thread { prevHandler.changePage() } }
        preferenceHandler()
    }

    private fun preferenceHandler() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        viewer.contentView.textSize = preferences.getString("textsize", "22")!!.toFloat()

        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        val themecolor = typedValue.data

        viewer.contentView.setTextColor(
            if (preferences.getBoolean("highcontrast", true)) {
                themecolor
            } else {
                if (themecolor == Color.WHITE) Color.LTGRAY else Color.GRAY
            }
        )

    }

    /** Returns data required to change pages from a sample Url, using either the configs or python bindings
     * @param url The url to obtain data from
     * @return Either an [emptyList] if invalid, or a list in the order [title, content, prevUrl, nextUrl, currentUrl]*/
    fun getUrlInfo(url: String): List<String?> {
        // Integration with Config table and Configurations
        val domain = url.split("/")[2].replace("www.", "")
        val data = DB.readItemFromConfigs(domain)
        if (data != null) {
            return try {
                // Error handling in case of networking error of some sort
                webdata(url, data.mainXPath, data.prevXPath, data.nextXPath)
            } catch (e: NetworkErrorException) {
                quit(e.message)
                emptyList()
            }
        }

        // Set up python stuff, and call UrlReading Class with a Url
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")

        return try {
            val instance = webpack.callAttr("UrlReading", url)
            val returnList = mutableListOf<String>()
            // Get data from UrlReading Instance
            with(returnList)
            {
                this.add(instance["title"].toString())
                this.add(instance["content"].toString())
                this.add(instance["prev"].toString())
                this.add(instance["next"].toString())
                this.add(url)
            }
            returnList

        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Updates UI using values obtained from [getUrlInfo] usually
     *  @param title Title of the chapter
     *  @param content Content of the chapter
     *  @param prevUrl The url to the previous chapter
     *  @param nextUrl The url to the next chapter
     *  @param current The url of the current chapter*/
    fun updateUi(
        title: String,
        content: String,
        prevUrl: String?,
        nextUrl: String?,
        current: String
    ) {
        viewer.scrollTitle.title = title
        viewer.contentView.text = content

        book.url = current
        DB.updateBook(book)

        // Hide buttons when they cannot be used
        viewer.prevButton.isVisible = prevUrl != null && prevUrl != "null"
        viewer.nextButton.isVisible = nextUrl != null && nextUrl != "null"

        // Title modifications to fit
        when {
            title.length > 60 -> viewer.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium)
            title.length > 20 -> viewer.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Large)
            else -> viewer.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded)
        }

        // Since the visibility is already changed, we don't have to check again
        if (viewer.prevButton.isVisible)
            prevHandler.prepPageChange(prevUrl!!)
        if (viewer.nextButton.isVisible)
            nextHandler.prepPageChange(nextUrl!!)

        viewer.contentScroll.scrollTo(0, 0)
    }

    /**A simple function to call to vibrate the phone
     * @param milis The milliseconds to vibrate for*/
    fun vibrate(milis: Int) {
        try {
            val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator())
                vibrator.vibrate(
                    VibrationEffect.createOneShot
                        (milis.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
                )
        } catch (e: Exception) {
        }
    }

    /** Returns to main activity with an optional error message
     * @param error The error message itself
     * @return Returns to the [FragmentMain] where the book lists are using [fragmentTransition], doesn't return anything*/
    fun quit(error: String? = null) {
        try {

            val activity: MainActivity = activity as MainActivity
            if (error != null) requireActivity().runOnUiThread {
                Toast.makeText(
                    activity,
                    error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            fragmentTransition(activity, activity.mainFrag, View.VISIBLE)

        } catch (e: Exception) {
        }
    }

    companion object {
        /** The only way you should initialize this class
         * @param book The book to read
         * @return new Instance of [FragmentRead]*/
        @JvmStatic
        fun newInstance(book: Book) =
            FragmentRead().apply {
                this.book = book
            }
    }
}

/** Handler for handling page changing logistics
 * @param readFragment The readFragment, which this uses the methods from
 */
class PageChangeHandler(private val readFragment: FragmentRead) {
    private lateinit var futureUrl: CompletableFuture<List<String?>>

    /** Preloads the url asynchronously
     *  @param url The url to preload*/
    fun prepPageChange(url: String) {
        futureUrl = CompletableFuture.supplyAsync { readFragment.getUrlInfo(url) }
    }

    /** Handles updating the UI of the fragment from the preloaded [futureUrl] defined in [prepPageChange]*/
    fun changePage() {
        readFragment.vibrate(100)
        val data = futureUrl.get()
        if (data.isNullOrEmpty()) {   // Error Handling in event of url break
            readFragment.quit("Unknown Error has occurred, may be linking to bad url")
            return
        }
        val (title, content, prevUrl, nextUrl, current) = data
        readFragment.requireActivity().runOnUiThread {
            readFragment.updateUi(
                title!!,
                content!!,
                prevUrl,
                nextUrl,
                current!!
            )
        }
    }

}