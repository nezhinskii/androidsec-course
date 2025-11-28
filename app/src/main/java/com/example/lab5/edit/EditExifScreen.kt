package com.example.lab5.edit

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab5.model.ExifData
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun EditExifScreen(
    currentUri: Uri,
    currentData: ExifData,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EditExifViewModel = viewModel()
) {
    val context = LocalContext.current

    var date by remember { mutableStateOf(currentData.dateOfCreation.orEmpty()) }
    var latitude by remember { mutableStateOf(currentData.latitude?.let { String.format(Locale.US, "%.6f", it) }.orEmpty()) }
    var longitude by remember { mutableStateOf(currentData.longitude?.let { String.format(Locale.US, "%.6f", it) }.orEmpty()) }
    var device by remember { mutableStateOf(currentData.device.orEmpty()) }
    var model by remember { mutableStateOf(currentData.deviceModel.orEmpty()) }

    var dateError by remember { mutableStateOf<String?>(null) }
    var latError by remember { mutableStateOf<String?>(null) }
    var lonError by remember { mutableStateOf<String?>(null) }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    LaunchedEffect(saveResult) {
        if (saveResult is SaveResult.Success) {
            onSaved()
            viewModel.clearResult()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Edit EXIF Tags", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date of creation") },
            modifier = Modifier.fillMaxWidth(),
            isError = dateError != null
        )
        dateError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = latError != null
        )
        latError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = lonError != null
        )
        lonError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = device, onValueChange = { device = it }, label = { Text("Device (Make)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Device model") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = {
                    dateError = null
                    latError = null
                    lonError = null

                    val dateRegex = Regex("^\\d{4}:(0[1-9]|1[0-2]):(0[1-9]|[12]\\d|3[01]) (0[0-9]|1\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$")
                    if (date.isNotBlank() && !date.matches(dateRegex)) {
                        dateError = "Invalid date format. Use YYYY:MM:DD HH:MM:SS"
                        return@Button
                    }

                    val lat = latitude.toDoubleOrNull()
                    if (latitude.isNotBlank() && (lat == null || lat < -90.0 || lat > 90.0)) {
                        latError = if (lat == null) "Invalid number" else "Latitude must be between -90 and 90"
                        return@Button
                    }

                    val lon = longitude.toDoubleOrNull()
                    if (longitude.isNotBlank() && (lon == null || lon < -180.0 || lon > 180.0)) {
                        lonError = if (lon == null) "Invalid number" else "Longitude must be between -180 and 180"
                        return@Button
                    }

                    val updated = ExifData(
                        dateOfCreation = date.ifBlank { null },
                        latitude = lat,
                        longitude = lon,
                        device = device.ifBlank { null },
                        deviceModel = model.ifBlank { null }
                    )
                    viewModel.saveExif(context, currentUri, updated)
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save")
            }
        }

        saveResult?.let { result ->
            Spacer(Modifier.height(16.dp))
            val text = when (result) {
                is SaveResult.Success -> "Saved successfully!"
                is SaveResult.Error -> result.message
            }
            val color = if (result is SaveResult.Success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Text(text, color = color)
        }
    }
}