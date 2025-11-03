package com.example.inventory.data.settings

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingsFlow: StateFlow<AppSettings>
    suspend fun updateHideSensitiveData(hide: Boolean)
    suspend fun updateDisableSharing(disable: Boolean)
    suspend fun updateUseDefaultQuantity(use: Boolean)
    suspend fun updateDefaultQuantity(quantity: Int)
}