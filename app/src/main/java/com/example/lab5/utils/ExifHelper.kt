package com.example.lab5.utils

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.lab5.model.ExifData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ExifHelper {

    fun readExifTags(context: Context, uri: Uri): ExifData {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                val date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

                val latLong = exif.latLong
                val latitude = latLong?.getOrNull(0)
                val longitude = latLong?.getOrNull(1)

                val device = exif.getAttribute(ExifInterface.TAG_MAKE)
                val model = exif.getAttribute(ExifInterface.TAG_MODEL)

                ExifData(
                    dateOfCreation = date,
                    latitude = latitude,
                    longitude = longitude,
                    device = device,
                    deviceModel = model
                )
            } ?: ExifData(error = "Failed to open image stream")
        } catch (e: Exception) {
            ExifData(error = e.message ?: "Unknown error reading EXIF")
        }
    }

    fun writeExifTags(context: Context, uri: Uri, updatedData: ExifData): String? {
        try {
            val tempFile = File.createTempFile("exif_temp", ".jpg", context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return "Failed to open input stream"

            val exif = ExifInterface(tempFile.absolutePath)
            if (updatedData.dateOfCreation != null) {
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, updatedData.dateOfCreation)
            } else {
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null)
            }

            if (updatedData.device != null) {
                exif.setAttribute(ExifInterface.TAG_MAKE, updatedData.device)
            } else {
                exif.setAttribute(ExifInterface.TAG_MAKE, null)
            }

            if (updatedData.deviceModel != null) {
                exif.setAttribute(ExifInterface.TAG_MODEL, updatedData.deviceModel)
            } else {
                exif.setAttribute(ExifInterface.TAG_MODEL, null)
            }

            if (updatedData.latitude != null && updatedData.longitude != null) {
                exif.setLatLong(updatedData.latitude, updatedData.longitude)
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
            }

            exif.saveAttributes()

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(tempFile).use { input ->
                    input.copyTo(output)
                }
            } ?: return "Failed to open output stream"

            tempFile.delete()

            return null
        } catch (e: Exception) {
            return e.message ?: "Unknown error writing EXIF"
        }
    }
}