package com.example.lab6

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab6.ui.StepsUiState
import kotlinx.coroutines.launch
import java.time.Instant

class StepsViewModel(
    private val healthManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = mutableStateOf(StepsUiState(isLoading = true))
    val uiState: State<StepsUiState> = _uiState

    init {
        loadData()
    }

    fun setDateRange(start: Instant, end: Instant) {
        _uiState.value = _uiState.value.copy(
            startTime = start,
            endTime = end,
            isLoading = true
        )
        loadData()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadData()
    }

    private fun loadData() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val records = healthManager.readSteps(state.startTime, state.endTime)
                val total = healthManager.getTotalSteps(state.startTime, state.endTime)

                _uiState.value = state.copy(
                    records = records,
                    totalSteps = total,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun addSteps(count: Long, start: Instant, end: Instant) {
        viewModelScope.launch {
            try {
                healthManager.insertSteps(count, start, end)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateSteps(uid: String, count: Long, start: Instant, end: Instant) {
        viewModelScope.launch {
            try {
                healthManager.updateSteps(uid, count, start, end)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteSteps(uid: String) {
        viewModelScope.launch {
            try {
                healthManager.deleteStepsById(uid)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}