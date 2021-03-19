package com.cobaltware.webscraper

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import kotlinx.android.synthetic.main.reader_view.view.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class ReadFragment : Fragment() {

    private var colId: Int = 0
    private val executor : Executor = Executors.newSingleThreadExecutor()
    lateinit var viewer : View

    companion object {
        @JvmStatic
        fun newInstance(url: String, colId: Int) =
                ReadFragment().apply {
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
        viewer = inflater.inflate(R.layout.reader_view, container, false)
        // Getting important values
        val url : String = arguments!!.getString("url")!!
        colId = arguments!!.getInt("col_id")

        //scrollable.movementMethod = ScrollingMovementMethod()

        setStaticUI()

        executor.execute { asyncUrlLoad(url) }

        return viewer
    }

    private fun setStaticUI()
    {
        viewer.prevButton.isVisible = false
        viewer.nextButton.isVisible = false

        viewer.scrollable.setNavigationOnClickListener {
            val fragmentTrans = requireFragmentManager().beginTransaction()
            fragmentTrans.replace(R.id.fragmentSpot, MainFragment())
            fragmentTrans.commit()
        }
    }


    private fun vibrate(milis: Int){
        val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator())
            vibrator.vibrate(
                    VibrationEffect.createOneShot
                    (milis.toLong(), VibrationEffect.EFFECT_HEAVY_CLICK))
    }
    private fun asyncUrlLoad(url: String)
    {
        vibrate(100)
        // Get the data
        Log.d("data", "running completable future")
        val completableFuture: CompletableFuture<List<String>> = CompletableFuture.supplyAsync { getUrlInfo(url) }
        Log.d("data", "getting data from completableFuture")
        val data : List<String> = completableFuture.get()
        Log.d("data", "Data obtained asyncUrlLoad")
        vibrate(150)
        // Update the gui
        requireActivity().runOnUiThread {
            Log.d("GUI", "Update UI from asyncUrlLoad")
            updateUi(data[0], data[1], data[2], data[3], data[4])
        }
    }
    private fun getUrlInfo(url: String) : List<String>
    {
        // Set up python stuff, and call UrlReading Class with a Url
        val inst = Python.getInstance()
        val webpack = inst.getModule("webdata")
        val instance = webpack.callAttr("UrlReading", url)
        val returnList = mutableListOf<String>()
        // Get data from UrlReading Instance
        returnList.add(instance["title"].toString())
        returnList.add(instance["content"].toString())
        returnList.add(instance["prev"].toString())
        returnList.add(instance["next"].toString())
        returnList.add(url)

        return returnList
    }

    private fun updateUi(title: String, content: String, prevUrl: String, nextUrl: String, current: String){
        viewer.scrollTitle.title = title
        viewer.contentView.text = content

        DB.modifyBooklistItem(null, colId, current, null)

        viewer.prevButton.isVisible = prevUrl != "null"
        viewer.nextButton.isVisible = nextUrl != "null"

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
            val prevData: CompletableFuture<List<String>> = CompletableFuture.supplyAsync { getUrlInfo(prevUrl) }
            // PrevButton click just updates the ui
            viewer.prevButton.setOnClickListener { executor.execute{
                vibrate(100)
                val data = prevData.get()
                requireActivity().runOnUiThread { updateUi(data[0], data[1], data[2], data[3], data[4]) }
            }}
        }
        if (viewer.nextButton.isVisible)
        {
            val nextData: CompletableFuture<List<String>> = CompletableFuture.supplyAsync { getUrlInfo(nextUrl) }
            // Same as prevButton click
            viewer.nextButton.setOnClickListener { executor.execute{
                vibrate(100)
                val data = nextData.get()
                requireActivity().runOnUiThread { updateUi(data[0], data[1], data[2], data[3], data[4]) }
            }}
        }

        viewer.contentScroll.scrollTo(0, 0)
    }
}