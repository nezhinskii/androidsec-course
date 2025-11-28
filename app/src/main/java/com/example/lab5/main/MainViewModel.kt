package com.example.lab5.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab5.model.ExifData
import com.example.lab5.utils.ExifHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _currentImageUri = MutableStateFlow<Uri?>(null)
    val currentImageUri: StateFlow<Uri?> = _currentImageUri.asStateFlow()

    private val _exifData = MutableStateFlow(ExifData())
    val exifData: StateFlow<ExifData> = _exifData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onImagePicked(uri: Uri, context: Context) {
        _currentImageUri.value = uri
        loadExif(uri, context)
    }

    fun refreshExif(context: Context) {
        _currentImageUri.value?.let { loadExif(it, context) }
    }

    private fun loadExif(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val data = ExifHelper.readExifTags(context, uri)
            _exifData.value = data
            _isLoading.value = false
        }
    }
}