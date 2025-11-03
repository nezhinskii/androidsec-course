package com.example.inventory.data.settings

data class AppSettings(
    val hideSensitiveData: Boolean = false,
    val disableSharing: Boolean = false,
    val useDefaultQuantity: Boolean = false,
    val defaultQuantity: Int = 0
)