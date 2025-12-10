package com.example.lab6.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.StepsRecord
import com.example.lab6.StepsViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StepsViewModel) {

    val uiState by viewModel.uiState

    var showBottomSheet by remember { mutableStateOf(false) }
    var editingRecordId by remember { mutableStateOf<String?>(null) }

    val editingRecord = editingRecordId?.let { id ->
        uiState.records.find { it.metadata.id == id }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Steps Tracker", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecordId = null
                    showBottomSheet = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add steps")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total steps",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = uiState.totalSteps.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DateRangePickerRow(
                        start = uiState.startTime,
                        end = uiState.endTime,
                        onRangeSelected = { start, end ->
                            viewModel.setDateRange(start, end)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No steps recorded in this period", style = MaterialTheme.typography.bodyLarge)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.records, key = { it.metadata.id }) { record ->
                        StepRecordCard(
                            record = record,
                            onEdit = {
                                editingRecordId = record.metadata.id
                                showBottomSheet = true
                            },
                            onDelete = {
                                viewModel.deleteSteps(record.metadata.id)
                            }
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            StepsBottomSheet(
                existingRecord = editingRecord,
                onSave = { count, start, end ->
                    if (editingRecordId != null) {
                        viewModel.updateSteps(editingRecordId!!, count, start, end)
                    } else {
                        viewModel.addSteps(count, start, end)
                    }
                    showBottomSheet = false
                    editingRecordId = null
                },
                onDismiss = {
                    showBottomSheet = false
                    editingRecordId = null
                }
            )
        }
    }
}

@Composable
private fun StepRecordCard(
    record: StepsRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        .withZone(ZoneId.systemDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.count} steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${formatter.format(record.startTime)} – ${formatter.format(record.endTime)}",
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}