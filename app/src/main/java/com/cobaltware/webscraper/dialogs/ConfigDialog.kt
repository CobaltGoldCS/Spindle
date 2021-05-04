package com.cobaltware.webscraper.dialogs

import android.os.Bundle
import android.view.*
import com.cobaltware.webscraper.ConfigAdapter
import com.cobaltware.webscraper.R
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.DB
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.menu_config.*
import kotlinx.android.synthetic.main.menu_config.view.*
import kotlin.concurrent.thread

class ConfigDialog(private var config : Config?, private var adapter : ConfigAdapter) : BottomSheetDialogFragment() {
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) : View? {

        DB.createTable("CONFIG",
                "(COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, DOMAIN VARCHAR(256), CONTENTXPATH VARCHAR(256), PREVXPATH VARCHAR(256), NEXTXPATH VARCHAR(256))")
        val view = inflater.inflate(R.layout.menu_config, container, true)

        setAllTexts(view)
        view.actionButton.setOnClickListener {onAction()}
        view.deleteButton.setOnClickListener {onDelete()}
        view.cancelButton.setOnClickListener {dismiss ()}

        return view
    }

    private fun onAction()
    {
        if (!guaranteeAllFields())
            return

        val modify = config != null

        val insertArgs = arrayOf(domainUrlInput.text.toString(), contentXpathInput.text.toString(), previousButtonXpathInput.text.toString(), nextButtonXpathInput.text.toString())
        val insertVals = arrayOf("DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH")
        val insertDict = createDictFromArrays(insertVals, insertArgs)

        when(modify)
        {
            true  -> DB.modifyItem("CONFIG", config!!.col_id, insertDict)
            false -> DB.insertItemIntoTable("CONFIG", insertDict)
        }
        updateAdapter()
        dismiss()
    }

    private fun onDelete()
    {
        thread{
            if (config != null) {
                DB.deleteUsingID("CONFIG", config!!.col_id)
                updateAdapter()
            }
            dismiss()
        }
    }
    private fun databaseToConfigs() = DB.readAllItems("CONFIG",
        listOf("COL_ID", "DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH")
            ).map { list -> Config(list[0].toInt(), list[1], list[2], list[3], list[4])}

    private fun updateAdapter()
    {
        val newList = databaseToConfigs()
        requireActivity().runOnUiThread{ adapter.changeItems(newList) }
    }

    private fun setAllTexts(v : View)
    {
        if (config == null)
            return
        v.domainUrlInput.setText(config!!.domain)
        v.nextButtonXpathInput.setText(config!!.nextXPath)
        v.previousButtonXpathInput.setText(config!!.prevXPath)
        v.contentXpathInput.setText(config!!.mainXPath)
    }

    /** Dispenses errors for text inputs if they are invalid
     * @return True if all fields are valid, false if not
     */
    private fun guaranteeAllFields() : Boolean
    {
        var valid = true
        listOf(domainUrlInput, nextButtonXpathInput, previousButtonXpathInput, contentXpathInput).forEach {
            if (it.text.isEmpty()) {
                it.error = "This field is invalid"
                valid = false
            }
        }
        return valid
    }

    /** Creates a map from list of keys and list of arguments
     * @param keys The keys for the map
     * @param args The arguments to match to the keys
     * @return The map with keys as keys to the args
     */
    private fun createDictFromArrays(keys : Array<String>, args : Array<String>) : Map<String, String> = keys.zip(args).toMap()

}