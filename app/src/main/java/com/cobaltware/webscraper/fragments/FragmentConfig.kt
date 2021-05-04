package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.ConfigAdapter
import com.cobaltware.webscraper.dialogs.ConfigDialog
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
        val viewer = inflater.inflate(R.layout.fragment_config, container, false)
        initRecycler(viewer)
        initBasicUi(viewer)
        return viewer
    }

    /** Populates and generally sets up the recyclerview
     * @param v The view that the recycler is part of
     */
    private fun initRecycler(v: View) {
        thread {
            // Populate recyclerview
            val fromDatabase = DB.readAllItems("CONFIG", listOf("COL_ID", "DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH"))
            val actualList = mutableListOf<Config>()
            fromDatabase.forEach { data ->
                actualList.add(Config(data[0].toInt(), data[1], data[2], data[3], data[4]))
            }

            configAdapter = object : ConfigAdapter(actualList.toList()) {
                override fun clickHandler(col_id: Int) {
                    val neededConfig = actualList.find {it.col_id == col_id}
                    addOrChangeDialog(neededConfig)
                }
            }

            requireActivity().runOnUiThread {
                v.configView.adapter = configAdapter
                v.configView.layoutManager = LinearLayoutManager(requireContext())
                v.configView.setHasFixedSize(true)
            }
        }
    }

    private fun initBasicUi(v: View) = v.addActionButton.setOnClickListener { addOrChangeDialog(null) }

    private fun addOrChangeDialog(config : Config?)
    {
        val dialog = ConfigDialog(config, configAdapter)
        dialog.show(requireActivity().supportFragmentManager, "Add New Config")
    }
}