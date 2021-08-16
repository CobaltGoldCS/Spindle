package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cobaltware.webscraper.ConfigAdapter
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.dialogs.ConfigDialog
import kotlinx.android.synthetic.main.fragment_config.view.*
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass.
 * Used for setting up configurations
 */
class FragmentConfig() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val viewer = inflater.inflate(R.layout.fragment_config, container, false)
        initRecycler(viewer)
        viewer.addActionButton.setOnClickListener { addOrChangeDialog(null) }
        return viewer
    }

    /** Populates and generally sets up the recyclerview
     * @param v The view that the recycler is part of
     */
    private fun initRecycler(v: View) = thread {

        val configAdapter = object : ConfigAdapter(emptyList()) {
            override fun clickHandler(row_id: Int) {
                val neededConfig = DB.readItemFromConfigs(row_id)
                addOrChangeDialog(neededConfig)
            }
        }

        requireActivity().runOnUiThread {
            v.configView.adapter = configAdapter
            v.configView.layoutManager = LinearLayoutManager(requireContext())
            v.configView.setHasFixedSize(true)

            DB.readAllConfigs.observe(viewLifecycleOwner, {
                configAdapter.changeItems(it)
            })
        }
    }

    /** Creates a [ConfigDialog] using the given [config] and displays it
     * @param config The config to put in the [ConfigDialog]*/
    private fun addOrChangeDialog(config: Config?) {
        val dialog = ConfigDialog(config)
        dialog.show(requireActivity().supportFragmentManager, "Add New Config")
    }
}