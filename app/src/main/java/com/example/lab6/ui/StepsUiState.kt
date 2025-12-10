package com.example.lab6.ui

import androidx.health.connect.client.records.StepsRecord
import java.time.Instant

data class StepsUiState(
    val records: List<StepsRecord> = emptyList(),
    val totalSteps: Long = 0L,
    val startTime: Instant = Instant.now().minusSeconds(7 * 24 * 3600),
    val endTime: Instant = Instant.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)