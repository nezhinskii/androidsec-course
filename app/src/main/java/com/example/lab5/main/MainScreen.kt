package com.example.lab5.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.lab5.model.ExifData
import com.example.lab5.main.MainViewModel
import com.example.lab5.edit.EditExifScreen

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    val uri by viewModel.currentImageUri.collectAsState()
    val exifData by viewModel.exifData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { result: Uri? ->
        result?.let { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            viewModel.onImagePicked(uri, context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "EXIF Editor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        uri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))
        }


        if (isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }

        Button(onClick = { launcher.launch(arrayOf("image/*")) }) {
            Text("Pick Image")
        }

        uri?.let {
            Spacer(Modifier.height(24.dp))

            ExifInfoCard(exifData)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showEditDialog = true },
                enabled = !exifData.hasError
            ) {
                Text("Edit EXIF Tags")
            }
        }

        if (showEditDialog && uri != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                text = {
                    EditExifScreen(
                        currentUri = uri!!,
                        currentData = exifData,
                        onSaved = {
                            showEditDialog = false
                            viewModel.refreshExif(context)
                        },
                        onCancel = { showEditDialog = false }
                    )
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ExifInfoCard(data: ExifData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("EXIF Information", style = MaterialTheme.typography.titleLarge)

            if (data.error != null) {
                Text("Error: ${data.error}", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            if (data.isEmpty) {
                Text("No EXIF data found")
                return@Column
            }

            data.dateOfCreation?.let { ExifRow("Date of creation", it) }
            data.latitude?.let { ExifRow("Latitude", String.format("%.6f°", it)) }
            data.longitude?.let { ExifRow("Longitude", String.format("%.6f°", it)) }
            data.device?.let { ExifRow("Device", it) }
            data.deviceModel?.let { ExifRow("Device model", it) }
        }
    }
}

@Composable
private fun ExifRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value)
    }
}