package com.cobaltware.webscraper.datahandling.useCases

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.datahandling.*
import com.cobaltware.webscraper.datahandling.repositories.ConfigRepository

class ConfigUseCase(context: Context) {
    private val repository: ConfigRepository
    val readAllConfigs: LiveData<List<Config>>

    init{
        val database = BookDatabase.getDatabase(context)

        repository = ConfigRepository(database.configDao())
        readAllConfigs = readAllConfigs()
    }

    private fun readAllConfigs() = repository.readAllConfigs
    fun readItemFromConfigs(rowId: Int) = repository.readItemFromConfigs(rowId)
}