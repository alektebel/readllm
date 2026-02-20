package com.readllm.app.repository

import com.readllm.app.database.ReadingSettingsDao
import com.readllm.app.model.ReadingSettings

class ReadingSettingsRepository(private val settingsDao: ReadingSettingsDao) {
    
    suspend fun getSettings(): ReadingSettings {
        return settingsDao.getSettings() ?: ReadingSettings().also {
            settingsDao.insertSettings(it)
        }
    }
    
    suspend fun updateSettings(settings: ReadingSettings) {
        settingsDao.updateSettings(settings)
    }
    
    suspend fun saveSettings(settings: ReadingSettings) {
        settingsDao.insertSettings(settings)
    }
}
