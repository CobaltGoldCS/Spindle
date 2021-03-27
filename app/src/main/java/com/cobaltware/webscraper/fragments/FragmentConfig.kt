package com.cobaltware.webscraper.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.ConfigAdapter
import com.cobaltware.webscraper.ConfigDialog
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.DB
import kotlinx.android.synthetic.main.fragment_config.view.*
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass.
 * Used for setting up configurations
 */
class FragmentConfig : Fragment() {
    private lateinit var configAdapter: ConfigAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        var viewer = inflater.inflate(R.layout.fragment_config, container, false)
        initRecycler(viewer)
        initBasicUi(viewer)
        setColors(viewer)
        return viewer
    }

    private fun initRecycler(v : View)
    { thread {
        val rawData = DB.readAllItems("CONFIG", listOf("COL_ID", "DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH"))
        val actualList = mutableListOf<Config>()
        for (data in rawData)
            actualList.add(Config(data[0].toInt(), data[1], data[2], data[3], data[4]))

        configAdapter = object : ConfigAdapter(actualList.toList()) {
            override fun clickHandler(col_id : Int)
            {
                val neededConfig = actualList.find { it.col_id == col_id }
                runAddDialog(neededConfig)
            }
        }
        requireActivity().runOnUiThread {
            v.configView.adapter = configAdapter
            v.configView.layoutManager = LinearLayoutManager(requireContext())
            v.configView.setHasFixedSize(true)
        }
    }
    }
    private fun initBasicUi(v : View)
    {
        v.addActionButton.setOnClickListener {
            runAddDialog(null)
        }
    }
    private fun setColors(v : View)
    {
        val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val backgroundColor : Int = if (darkMode) R.color.background else Color.WHITE
        val context  = requireContext()
        v.configView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
    }
    private fun runAddDialog(config : Config?)
    {
        val dialog = ConfigDialog(requireContext(), config, configAdapter)
        dialog.show()
    }
}