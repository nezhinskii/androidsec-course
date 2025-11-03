package com.example.inventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.settings.SettingsRepository
import com.example.inventory.data.settings.AppSettings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AppSettings> = settingsRepository.settingsFlow

    fun onHideSensitiveDataChanged(checked: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateHideSensitiveData(checked)
        }
    }

    fun onDisableSharingChanged(checked: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDisableSharing(checked)
        }
    }

    fun onUseDefaultQuantityChanged(checked: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUseDefaultQuantity(checked)
        }
    }

    fun onDefaultQuantityChanged(text: String) {
        val quantity = text.toIntOrNull()?.coerceAtLeast(0) ?: return
        viewModelScope.launch {
            settingsRepository.updateDefaultQuantity(quantity)
        }
    }
}