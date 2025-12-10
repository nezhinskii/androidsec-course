package com.example.lab6.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.StepsRecord
import androidx.compose.ui.text.input.KeyboardType
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsBottomSheet(
    existingRecord: StepsRecord?,
    onSave: (count: Long, start: Instant, end: Instant) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = existingRecord != null

    var stepsText by remember { mutableStateOf(existingRecord?.count?.toString() ?: "") }
    var startTime by remember { mutableStateOf(existingRecord?.startTime ?: Instant.now()) }
    var endTime by remember { mutableStateOf(existingRecord?.endTime ?: Instant.now().plusSeconds(3600)) }  // +1 час по умолчанию

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 24.dp)) {
            Text(
                text = if (isEdit) "Edit Steps Record" else "Add Steps Record",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = MaterialTheme.typography.headlineSmall.fontWeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                label = { Text("Number of steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateTimePickerButton(
                label = "Start time",
                currentTime = startTime,
                onTimeSelected = { newTime -> startTime = newTime }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateTimePickerButton(
                label = "End time",
                currentTime = endTime,
                onTimeSelected = { newTime -> endTime = newTime }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val count = stepsText.toLongOrNull() ?: return@Button
                        if (startTime.isAfter(endTime)) return@Button
                        onSave(count, startTime, endTime)
                    },
                    enabled = stepsText.toLongOrNull() != null && !startTime.isAfter(endTime)
                ) {
                    Text("Save")
                }
            }
        }
    }
}