package com.cobaltware.webscraper.fragments

import android.accounts.NetworkErrorException
import android.content.Context
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.webhandlers.webdata
import com.cobaltware.webscraper.viewcontrollers.ChapterChangeHandler
import com.cobaltware.webscraper.viewcontrollers.ReadViewController
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_read.*
import kotlinx.android.synthetic.main.fragment_read.view.*
import kotlin.concurrent.thread


class FragmentRead(private val book: Book) : Fragment() {

    lateinit var viewController: ReadViewController

    private val nextHandler = ChapterChangeHandler(this)
    private val prevHandler = ChapterChangeHandler(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_read, container, false)
        viewController = ReadViewController(view)

        // Manipulate the GUI
        setListeners(view)
        thread { preferenceHandler() }

        // First time run
        thread {
            vibrate(100)
            Log.i("data", "Obtaining data")
            val data: List<String?> = getUrlInfo(book.url)

            if (data.isNullOrEmpty()) {
                // Error handling
                Log.e("Domain Error", "Domain may not be properly supported; exiting")
                quit("This domain / url is not properly supported in the configs; or it may not exist anymore")
                return@thread
            }

            // Update the gui
            Log.i("GUI", "Update UI from asyncUrlLoad")
            try {
                requireActivity().runOnUiThread {
                    updateUi(data[0]!!, data[1]!!, data[2], data[3])
                }
            } catch (e: Exception) {
                Log.i("FragmentRead", "This fragment should not exist")
            }
            vibrate(150)
        }
        return view
    }

    /** Sets ClickHandler actions
     * @param view The [View] inflated by [FragmentRead]**/
    private fun setListeners(view: View) {
        view.scrollable.setNavigationOnClickListener { quit() }
        view.nextButton.setOnClickListener { thread { nextHandler.changePage() } }
        view.prevButton.setOnClickListener { thread { prevHandler.changePage() } }
    }

    /**Initializes preferences and configures ui elements based on those preferences*/
    private fun preferenceHandler() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        // Obtain theme color
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        val themeColor = typedValue.data

        requireActivity().runOnUiThread {
            viewController.setPreferenceUI(preferences, themeColor)
        }

    }

    /** Reads a book using values defined from [data]
     * @param url The url to obtain data from
     * @param data The configuration information for data collection
     * @return A list containing (title, main text content, previous url, next url, current url) */
    private fun readBookFromConfig(url: String, data: Config): List<String?> {
        return try {
            // Error handling in case of networking error of some sort
            webdata(url, data.mainXPath, data.prevXPath, data.nextXPath)
        } catch (e: NetworkErrorException) {
            quit(e.message)
            emptyList()
        }
    }

    /** Reads a book using "UrlReading" defined in python/webdata.py
     * @param url The url to obtain data from
     * @return A list containing (title, main text content, previous url, next url, current url) */
    private fun readBookWithPython(url: String): List<String> {
        // Set up python stuff, and call UrlReading Class with a Url
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")

        return try {
            val instance = webpack.callAttr("UrlReading", url)
            val returnList = mutableListOf<String>()
            // Get data from UrlReading Instance
            returnList.apply {
                this.add(instance["title"].toString())
                this.add(instance["content"].toString())
                this.add(instance["prev"].toString())
                this.add(instance["next"].toString())
            }
            returnList

        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Returns data required to change pages from a sample Url, using either the configs or python bindings
     * @param url The url to obtain data from
     * @return Either an [emptyList] if invalid, or a list in the order [title, content, prevUrl, nextUrl, currentUrl]*/
    fun getUrlInfo(url: String): List<String?> {
        // Integration with Config table and Configurations
        val domain = url.split("/")[2].replace("www.", "")
        val data = DB.readItemFromConfigs(domain)
        return if (data != null) {
            // Prefers user inputted configs
            readBookFromConfig(url, data)
        } else readBookWithPython(url)
    }

    /** Updates UI using values obtained from [getUrlInfo] usually
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
        requireActivity().runOnUiThread {
            viewController.updateUi(title, content, prevUrl, nextUrl)
        }

        val view = viewController.view
        // Since the visibility is already changed, we don't have to check again
        if (view.prevButton.isVisible) prevHandler.prepPageChange(prevUrl!!)
        if (view.nextButton.isVisible) nextHandler.prepPageChange(nextUrl!!)

        DB.updateBook(book)
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
     * @param error The error message text
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
}