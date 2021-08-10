package com.cobaltware.webscraper.dialogs

import android.os.Bundle
import android.view.*
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.datahandling.Config
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.menu_config.*
import kotlinx.android.synthetic.main.menu_config.view.*
import kotlin.concurrent.thread

class ConfigDialog(private val config: Config?) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.menu_config, container, true)

        setAllTexts(view)
        view.actionButton.setOnClickListener { onAction() }
        view.deleteButton.setOnClickListener { onDelete() }
        view.cancelButton.setOnClickListener { dismiss() }

        return view
    }

    /**Modifies the given [config] if there is one, or creates a new config and adds it to the database. Also makes sure all fields are valid using [guaranteeAllFields]
     * @see guaranteeAllFields*/
    private fun onAction() {
        if (!guaranteeAllFields())
            return

        val modify = config != null

        val insertArg = Config(
            if (modify) config!!.row_id else 0,
            domainUrlInput.text.toString(),
            contentXpathInput.text.toString(),
            previousButtonXpathInput.text.toString(),
            nextButtonXpathInput.text.toString()
        )

        when (modify) {
            true -> DB.updateConfig(insertArg)
            false -> DB.addConfig(insertArg)
        }
        dismiss()
    }

    /**Deletes the current [config], or dismisses if there is nothing to delete*/
    private fun onDelete() {
        thread {
            if (config != null) {
                DB.deleteConfig(config)
            }
            dismiss()
        }
    }


    /**Sets all of the text input boxes automatically using the given [config]
     * @param v The view used to reference the text input boxes*/
    private fun setAllTexts(v: View) {
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
    private fun guaranteeAllFields(): Boolean {
        var valid = true
        listOf(
            domainUrlInput,
            nextButtonXpathInput,
            previousButtonXpathInput,
            contentXpathInput
        ).forEach {
            if (it.text.isEmpty()) {
                it.error = "This field is invalid"
                valid = false
            }
        }
        return valid
    }

}