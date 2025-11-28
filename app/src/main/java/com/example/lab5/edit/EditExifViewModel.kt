package com.example.lab5.edit

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

sealed interface SaveResult {
    data object Success : SaveResult
    data class Error(val message: String) : SaveResult
}

class EditExifViewModel : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    val saveResult: StateFlow<SaveResult?> = _saveResult.asStateFlow()

    fun saveExif(context: Context, uri: Uri, updatedData: ExifData) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveResult.value = null

            val error = ExifHelper.writeExifTags(context, uri, updatedData)
            _saveResult.value = if (error == null) {
                SaveResult.Success
            } else {
                SaveResult.Error("Error: $error")
            }

            _isSaving.value = false
        }
    }

    fun clearResult() {
        _saveResult.value = null
    }
}