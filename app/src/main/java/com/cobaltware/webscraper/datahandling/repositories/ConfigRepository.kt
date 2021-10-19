package com.cobaltware.webscraper.datahandling.repositories

import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.datahandling.ConfigDao

class ConfigRepository(private val configDao: ConfigDao) {
    val readAllConfigs: LiveData<List<Config>> = configDao.readAllFromConfigs()

    suspend fun addConfig(config: Config) {
        configDao.addConfig(config)
    }

    suspend fun updateConfig(config: Config) {
        configDao.updateConfig(config)
    }

    suspend fun deleteConfig(config: Config) {
        configDao.deleteConfig(config)
    }


    fun readItemFromConfigs(domain: String) =
        try {
            configDao.readItemFromConfigs(domain)[0]
        } catch (e: IndexOutOfBoundsException) {
            null
        }

    fun readItemFromConfigs(row_id: Int) =
        configDao.readItemFromConfigs(row_id)[0]
}