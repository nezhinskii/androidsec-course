package com.example.lab6

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StepsViewModelFactory(
    private val healthManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepsViewModel(healthManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}