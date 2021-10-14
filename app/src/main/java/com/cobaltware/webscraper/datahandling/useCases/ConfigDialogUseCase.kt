package com.cobaltware.webscraper.datahandling.useCases

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cobaltware.webscraper.datahandling.BookDatabase
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.repositories.ConfigRepository
import kotlinx.coroutines.launch

class ConfigDialogUseCase(context: Context) : AndroidViewModel(Application()) {
    private val repository: ConfigRepository

    init {
        val database = BookDatabase.getDatabase(context)

        repository = ConfigRepository(database.configDao())
    }

    fun updateConfig(config: Config) = viewModelScope.launch {
        repository.updateConfig(config)
    }

    fun addConfig(config: Config) = viewModelScope.launch {
        repository.addConfig(config)
    }

    fun deleteConfig(config: Config) = viewModelScope.launch {
        repository.deleteConfig(config)
    }


}