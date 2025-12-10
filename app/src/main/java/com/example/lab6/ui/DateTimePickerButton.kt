package com.example.lab6.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())
private fun Instant.format(formatter: DateTimeFormatter): String = formatter.format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerButton(
    label: String,
    currentTime: Instant,
    onTimeSelected: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(currentTime.atZone(ZoneId.systemDefault()).toLocalDate()) }

    Button(
        onClick = { showDatePicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("$label: ${currentTime.format(dateTimeFormatter)}")
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = currentTime.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val currentLocalTime = currentTime.atZone(ZoneId.systemDefault()).toLocalTime()
        val timeState = rememberTimePickerState(
            initialHour = currentLocalTime.hour,
            initialMinute = currentLocalTime.minute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = selectedDate.atTime(timeState.hour, timeState.minute)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .coerceAtMost(Instant.now())
                    onTimeSelected(newTime)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }
}