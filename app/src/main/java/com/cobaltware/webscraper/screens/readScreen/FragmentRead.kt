package com.cobaltware.webscraper.screens.readScreen

import android.accounts.NetworkErrorException
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.chaquo.python.Python
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.databinding.FragmentReadBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.Response
import com.cobaltware.webscraper.datahandling.useCases.ReadUseCase
import com.cobaltware.webscraper.datahandling.webhandlers.webdata
import com.cobaltware.webscraper.general.fragmentTransition
import com.cobaltware.webscraper.screens.mainScreen.FragmentMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.concurrent.thread


class FragmentRead(private var book: Book) : Fragment() {

    private lateinit var viewController: ReadViewController

    private val dataHandler: ReadUseCase by lazy { ReadUseCase(requireContext()) }

    private val nextHandler = ChapterChangeHandler(this)
    private val prevHandler = ChapterChangeHandler(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        val view = FragmentReadBinding.inflate(layoutInflater)
        viewController = ReadViewController(view)
        // Manipulate the GUI
        setListeners(view)
        thread { preferenceHandler() }

        // First time run
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thread {
            vibrate(100)
            Log.i("data", "Obtaining data")
            runBlocking {
                // The handler used here does not matter, this is just for the initial data obtainment
                prevHandler.prepPageChange(book.url)
                prevHandler.changePage()
            }
            vibrate(150)
        }
    }

    /** Sets ClickHandler actions
     * @param view The [View] inflated by [FragmentRead]**/
    private fun setListeners(view: FragmentReadBinding) {
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
    private fun readBookFromConfig(url: String, data: Config): Response<List<String?>> {
        return try {
            // Error handling in case of networking error of some sort
            webdata(url, data.mainXPath, data.prevXPath, data.nextXPath)
        } catch (e: NetworkErrorException) {
            Response.Failure("Could not get data from url")
        }
    }

    /** Reads a book using "UrlReading" defined in python/webdata.py
     * @param url The url to obtain data from
     * @return A list containing (title, main text content, previous url, next url, current url) */
    private fun readBookWithPython(url: String): Response<List<String?>> {
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
                this.add(url)
            }
            Response.Success(returnList)

        } catch (e: Exception) {
            Response.Failure("There is no configuration to access this url")
        }
    }

    /** Returns data required to change pages from a sample Url, using either the configs or python bindings
     * @param url The url to obtain data from
     * @return Either an [emptyList] if invalid, or a list in the order [title, content, prevUrl, nextUrl, currentUrl]*/
    fun getUrlInfo(url: String): Response<List<String?>> {
        // Integration with Config table and Configurations
        val domain = URL(url).host.replace("www.", "")
        val config = dataHandler.readItemFromConfigs(domain)
        return if (config != null) {
            // Prefers user inputted configs
            readBookFromConfig(url, config)
        } else readBookWithPython(url)
    }

    /** Updates UI using values obtained from [getUrlInfo] usually
     *  @param title Title of the chapter
     *  @param content Content of the chapter
     *  @param prevUrl The url to the previous chapter
     *  @param nextUrl The url to the next chapter*/
    suspend fun updateUi(
        title: String,
        content: String,
        prevUrl: String?,
        nextUrl: String?,
        current: String?,
    ) = withContext(Dispatchers.Main) {
        viewController.updateUi(title, content, prevUrl, nextUrl)

        thread {
            val view = viewController.view
            // Since the visibility is already changed, we don't have to check again
            if (view.prevButton.isVisible) prevHandler.prepPageChange(prevUrl!!)
            if (view.nextButton.isVisible) nextHandler.prepPageChange(nextUrl!!)
            book = book.copy(url = current!!)
            dataHandler.updateBook(book)
        }
    }

    /**A simple function to call to vibrate the phone
     * @param millis The milliseconds to vibrate for*/
    fun vibrate(millis: Int) {
        try {
            val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator())
                vibrator.vibrate(
                    VibrationEffect.createOneShot
                        (millis.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
                )
        } catch (e: Exception) {
            // This just makes sure the vibration utility doesn't inadvertently break the app
        }
    }

    /** Returns to main activity with an optional error message
     * @param error The error message text
     * @return Returns to the [FragmentMain] where the book lists are using [fragmentTransition], doesn't return anything*/
    fun quit(error: String? = null) {
        try {
            if (error != null) requireActivity().runOnUiThread {
                Toast.makeText(
                    activity,
                    error,
                    Toast.LENGTH_SHORT
                ).show()
            }
            fragmentTransition(requireContext(), FragmentMain(), View.VISIBLE)
        } catch (e: Exception) {
            // Just ignore the exception because we are going back to the main fragment
        }
    }
}