package com.example.inventory.data.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val encryptedPrefs: EncryptedPrefsManager
) : SettingsRepository {
    private val _settingsFlow = MutableStateFlow(loadInitialSettings())
    override val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadInitialSettings(): AppSettings = AppSettings(
        hideSensitiveData = encryptedPrefs.getBoolean(PrefKeys.HIDE_SENSITIVE_DATA, false),
        disableSharing = encryptedPrefs.getBoolean(PrefKeys.DISABLE_SHARING, false),
        useDefaultQuantity = encryptedPrefs.getBoolean(PrefKeys.USE_DEFAULT_QUANTITY, false),
        defaultQuantity = encryptedPrefs.getInt(PrefKeys.DEFAULT_QUANTITY, 0)
    )

    override suspend fun updateHideSensitiveData(hide: Boolean) {
        encryptedPrefs.putBoolean(PrefKeys.HIDE_SENSITIVE_DATA, hide)
        _settingsFlow.value = _settingsFlow.value.copy(hideSensitiveData = hide)
    }

    override suspend fun updateDisableSharing(disable: Boolean) {
        encryptedPrefs.putBoolean(PrefKeys.DISABLE_SHARING, disable)
        _settingsFlow.value = _settingsFlow.value.copy(disableSharing = disable)
    }

    override suspend fun updateUseDefaultQuantity(use: Boolean) {
        encryptedPrefs.putBoolean(PrefKeys.USE_DEFAULT_QUANTITY, use)
        _settingsFlow.value = _settingsFlow.value.copy(useDefaultQuantity = use)
    }

    override suspend fun updateDefaultQuantity(quantity: Int) {
        encryptedPrefs.putInt(PrefKeys.DEFAULT_QUANTITY, quantity)
        _settingsFlow.value = _settingsFlow.value.copy(defaultQuantity = quantity)
    }
}