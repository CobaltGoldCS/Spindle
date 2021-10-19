package com.cobaltware.webscraper.screens.configScreen

import android.os.Bundle
import android.view.*
import com.cobaltware.webscraper.databinding.MenuConfigBinding
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.useCases.ConfigDialogUseCase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.concurrent.thread

class ConfigDialog(private val config: Config?) :
    BottomSheetDialogFragment() {

    private val dataHandler: ConfigDialogUseCase by lazy { ConfigDialogUseCase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = MenuConfigBinding.inflate(layoutInflater)

        setAllTexts(view)
        view.actionButton.setOnClickListener { onAction(view) }
        view.deleteButton.setOnClickListener { onDelete() }
        view.cancelButton.setOnClickListener { dismiss() }

        return view.root
    }


    /**Modifies the given [config] if there is one, or creates a new config and adds it to the database. Also makes sure all fields are valid using [guaranteeAllFields]
     * @see guaranteeAllFields*/
    private fun onAction(view: MenuConfigBinding) {
        if (!guaranteeAllFields(view))
            return

        val modify = config != null

        val insertArg = Config(
            if (modify) config!!.row_id else 0,
            view.domainUrlInput.text.toString(),
            view.contentXpathInput.text.toString(),
            view.previousButtonXpathInput.text.toString(),
            view.nextButtonXpathInput.text.toString()
        )

        when (modify) {
            true -> dataHandler.updateConfig(insertArg)
            false -> dataHandler.addConfig(insertArg)
        }
        dismiss()
    }

    /**Deletes the current [config], or dismisses if there is nothing to delete*/
    private fun onDelete() {
        thread {
            if (config != null)
                dataHandler.deleteConfig(config)
            dismiss()
        }
    }


    /**Sets all of the text input boxes automatically using the given [config]
     * @param v The view used to reference the text input boxes*/
    private fun setAllTexts(v: MenuConfigBinding) {
        if (config != null) {

            v.domainUrlInput.setText(config.domain)
            v.nextButtonXpathInput.setText(config.nextXPath)
            v.previousButtonXpathInput.setText(config.prevXPath)
            v.contentXpathInput.setText(config.mainXPath)
        }
    }

    /** Dispenses errors for text inputs if they are invalid
     * @return True if all fields are valid, false if not
     */
    private fun guaranteeAllFields(view: MenuConfigBinding): Boolean {
        var valid = true
        listOf(
            view.domainUrlInput,
            view.nextButtonXpathInput,
            view.previousButtonXpathInput,
            view.contentXpathInput
        ).forEach {
            if (it.text.isEmpty()) {
                it.error = "This field is invalid"
                valid = false
            }
        }
        return valid
    }

}