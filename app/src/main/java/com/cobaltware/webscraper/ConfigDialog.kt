package com.cobaltware.webscraper

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.DB
import kotlinx.android.synthetic.main.menu_config.*

class ConfigDialog(context : Context, private var config : Config?, private var adapter : ConfigAdapter) : Dialog(context){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DB.createTable("CONFIG",
            "(COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, DOMAIN VARCHAR(256), CONTENTXPATH VARCHAR(256), PREVXPATH VARCHAR(256), NEXTXPATH VARCHAR(256))")
        setContentView(R.layout.menu_config)
        setAllTexts()
        actionButton.setOnClickListener {onActionClick()}
        deleteButton.setOnClickListener {onDelete()}
        cancelButton.setOnClickListener {dismiss() }

    }
    fun onActionClick()
    {
        val filled = guaranteeAllFields()
        if (!filled)
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
    }

    fun onDelete()
    {
        if (config != null) {
            DB.deleteUsingID("CONFIG", config!!.col_id)
            updateAdapter()
        }
        dismiss()
    }
    private fun makeConfigFromList(list : List<String>) = Config(list[0].toInt(), list[1], list[2], list[3], list[4])
    private fun updateAdapter()
    {
        val newList = DB.readAllItems("CONFIG",
            listOf("COL_ID", "DOMAIN", "CONTENTXPATH", "PREVXPATH", "NEXTXPATH")
            ).map { makeConfigFromList(it) }
        adapter.changeItems(newList)
    }

    private fun setAllTexts()
    {
        if (config == null)
            return
        domainUrlInput.setText(config!!.domain)
        nextButtonXpathInput.setText(config!!.nextXPath)
        previousButtonXpathInput.setText(config!!.prevXPath)
        contentXpathInput.setText(config!!.mainXPath)
    }

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