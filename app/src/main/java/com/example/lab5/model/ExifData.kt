package com.example.lab5.model

data class ExifData(
    val dateOfCreation: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val device: String? = null,
    val deviceModel: String? = null,
    val error: String? = null
) {
    val hasError = error != null
    val isEmpty = dateOfCreation == null &&
            latitude == null &&
            longitude == null &&
            device == null &&
            deviceModel == null &&
            error == null
}