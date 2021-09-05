package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.FragmentConfigBinding
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.dialogs.ConfigDialog
import com.cobaltware.webscraper.viewcontrollers.ConfigViewController


/**
 * A simple [Fragment] subclass.
 * Used for setting up configurations
 */
class FragmentConfig : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val viewer = FragmentConfigBinding.inflate(inflater)
        //initRecycler(viewer)
        viewer.addActionButton.setOnClickListener { addOrChangeDialog(null) }
        viewer.configView.setContent {
            ConfigViewController(requireContext()).ConfigRecycler(
                data = DB.readAllConfigs,
                clickHandler = {
                    val neededConfig = DB.readItemFromConfigs(it.row_id)
                    addOrChangeDialog(neededConfig)
                }
            )
        }
        return viewer.root
    }

    /** Creates a [ConfigDialog] using the given [config] and displays it
     * @param config The config to put in the [ConfigDialog]*/
    private fun addOrChangeDialog(config: Config?) {
        val dialog = ConfigDialog(config)
        dialog.show(requireActivity().supportFragmentManager, "Add New Config")
    }
}