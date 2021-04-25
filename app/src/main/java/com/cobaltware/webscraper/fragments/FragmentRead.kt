package com.cobaltware.webscraper.fragments

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.cobaltware.webscraper.MainActivity
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.datahandling.DB
import com.cobaltware.webscraper.datahandling.webhandlers.webdata
import kotlinx.android.synthetic.main.fragment_read.*
import kotlinx.android.synthetic.main.fragment_read.view.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class FragmentRead : Fragment() {

    private var colId: Int = 0
    private val executor : Executor = Executors.newSingleThreadExecutor()
    lateinit var viewer : View

    companion object {
        @JvmStatic
        fun newInstance(url: String, colId: Int) =
                FragmentRead().apply {
                    arguments = Bundle().apply {
                        putString("url", url)
                        putInt("col_id", colId)
                    }
                }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewer = inflater.inflate(R.layout.fragment_read, container, false)
        // Getting important values
        val url : String = arguments!!.getString("url")!!
        colId = arguments!!.getInt("col_id")


        setStaticUI()

        // First time run
        executor.execute {
            vibrate(100)
            Log.d("data", "running completable future to obtain data")
            val completableFuture: CompletableFuture<List<String?>> = CompletableFuture.supplyAsync { getUrlInfo(url) }
            val data : List<String?> = completableFuture.get()

            if (data.isNullOrEmpty()) {
                // Error handling
                Log.e("Domain Error","Domain may not be properly supported; exiting")
                quit("This domain / url is not properly supported in the configs")

                return@execute
            }

            Log.d("data", "Data obtained from future")
            vibrate(150)
            // Update the gui
            requireActivity().runOnUiThread {
                Log.d("GUI", "Update UI from asyncUrlLoad")
                updateUi(data[0]!!, data[1]!!, data[2], data[3], data[4]!!)
            }
        }

        return viewer
    }

    private fun setStaticUI()
    {
        viewer.prevButton.isVisible = false
        viewer.nextButton.isVisible = false

        viewer.scrollable.setNavigationOnClickListener {
            quit()
        }
    }

    private fun getUrlInfo(url: String) : List<String?>
    {
        // Integration with Config table and Configurations
        val domain = url.split("/")[2].replace("www.", "")
        val data = DB.getConfigFromDomain("CONFIG", domain)
        if (!data.isNullOrEmpty())
            return webdata(url, data[2], data[3], data[4])

        // Set up python stuff, and call UrlReading Class with a Url
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")
        try {
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
            return returnList

        }catch (e: Exception){return emptyList()}
    }

    private fun updateUi(title: String, content: String, prevUrl: String?, nextUrl: String?, current: String){
        viewer.scrollTitle.title = title
        viewer.contentView.text = content

        DB.modifyItem(null, colId, arrayOf("URL").zip(arrayOf(current)).toMap())

        viewer.prevButton.isVisible = prevUrl != null && prevUrl != "null"
        viewer.nextButton.isVisible = nextUrl != null && nextUrl != "null"

        // Title modifications to fit
        when {
            title.length > 60 -> viewer.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium)
            title.length > 20 -> viewer.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Large)
            else -> viewer.scrollTitle.setExpandedTitleTextAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded)
        }

        // Preload next and previous pages
        // Full process of this mirrors asyncUrlLoad without the logs
        if (viewer.prevButton.isVisible)
        {
            val prevData: CompletableFuture<List<String?>> = CompletableFuture.supplyAsync { getUrlInfo(prevUrl!!) }
            // PrevButton click just updates the ui
            viewer.prevButton.setOnClickListener { executor.execute{
                vibrate(100)
                val data = prevData.get()
                if (data.isNullOrEmpty())
                {   // Error Handling in event of url break
                    quit("Unknown Error has occurred, may be linking to bad url")
                    return@execute
                }
                requireActivity().runOnUiThread { updateUi(data[0]!!, data[1]!!, data[2], data[3], data[4]!!) }
            }}
        }
        if (viewer.nextButton.isVisible)
        {
            val nextData: CompletableFuture<List<String?>> = CompletableFuture.supplyAsync { getUrlInfo(nextUrl!!) }
            // Same as prevButton click
            viewer.nextButton.setOnClickListener { executor.execute{
                vibrate(100)
                val data = nextData.get()
                if (data.isNullOrEmpty())
                {   // Error Handling in event of url break
                    quit("Unknown Error has occurred, may be linking to bad url")
                    return@execute
                }
                requireActivity().runOnUiThread { updateUi(data[0]!!, data[1]!!, data[2], data[3], data[4]!!) }
            }}
        }

        viewer.contentScroll.scrollTo(0, 0)
    }

    private fun vibrate(milis: Int) {
        try {
            val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator())
                vibrator.vibrate(VibrationEffect.createOneShot
                (milis.toLong(), VibrationEffect.EFFECT_HEAVY_CLICK))
        } catch (e: Exception) {}
    }
    private fun quit(error: String? = null)
    {
        val activity : MainActivity = activity as MainActivity
        if (error != null) Toast.makeText(activity,  error, Toast.LENGTH_SHORT).show()
        fragmentTransition(activity, FragmentMain(), View.VISIBLE)
    }
}