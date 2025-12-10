package com.example.lab6.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DateRangePickerRow(
    start: Instant,
    end: Instant,
    onRangeSelected: (Instant, Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Button(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Period: ${start.toPrettyDate()} – ${end.toPrettyDate()}"
        )
    }

    if (showPicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = start.toEpochMilli(),
            initialSelectedEndDateMillis = end.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newStart = state.selectedStartDateMillis?.let { Instant.ofEpochMilli(it) } ?: start
                    val newEnd = state.selectedEndDateMillis?.let { Instant.ofEpochMilli(it) } ?: end
                    onRangeSelected(newStart, newEnd)
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(state = state)
        }
    }
}

private fun Instant.toPrettyDate(): String {
    return this.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM"))
}