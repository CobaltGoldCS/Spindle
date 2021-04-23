package com.cobaltware.webscraper

import android.os.Bundle
import android.view.*
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
        view.actionButton.setOnClickListener {onClick() }
        view.deleteButton.setOnClickListener {onDelete()}
        view.cancelButton.setOnClickListener {dismiss() }
        return view
    }

    private fun onClick()
    {
        if (!guaranteeAllFields())
            return
        val modify = config != null

        val insertArgs = arrayOf(domainUrlInput.text.toString(), contentXpathInput.text.toString(), previousButtonXpathInput.text.toString(), nextButtonXpathInput.text.toString())
        val insertVals = arrayOf("DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH")

        if (!modify)
        {

            val insert = createDictFromArrays(insertVals, insertArgs)
            DB.insertItemIntoTable("CONFIG", insert)
            updateAdapter()
        }
        else{
            DB.modifyItem("CONFIG", config!!.col_id, createDictFromArrays(insertVals, insertArgs))
            updateAdapter()
        }
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
    // Returns true if all fields are filled, else false
    private fun guaranteeAllFields() : Boolean
    {
        if (domainUrlInput.text.isEmpty()||
            nextButtonXpathInput.text.isEmpty()||
            previousButtonXpathInput.text.isEmpty()||
            contentXpathInput.text.isEmpty())
        {
            domainUrlInput.error = "All fields are required"
            nextButtonXpathInput.error = "All fields are required"
            previousButtonXpathInput.error = "All fields are required"
            contentXpathInput.error = "All fields are required"
            return false
        }
        return true
    }

    private fun createDictFromArrays(keys : Array<String>, args : Array<String>) : Map<String, String> = keys.zip(args).toMap()

}