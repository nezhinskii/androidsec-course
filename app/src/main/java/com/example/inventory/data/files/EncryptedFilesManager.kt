package com.example.inventory.data.files

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class EncryptedFilesManager(private val context: Context) {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val tempFileName = "encrypted.tmp"

    fun encryptToStream(plainBytes: ByteArray, outputStream: OutputStream) {
        val tempFile = File(context.cacheDir, tempFileName)
        try {
            tempFile.delete()
            val encryptedFile = EncryptedFile.Builder(
                context,
                tempFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileOutput().use { it.write(plainBytes) }
            tempFile.inputStream().use { it.copyTo(outputStream) }

        } finally {
            tempFile.delete()
        }
    }

    fun decryptFromStream(inputStream: InputStream): ByteArray {
        val tempFile = File(context.cacheDir, tempFileName)
        try {
            tempFile.delete()
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            val encryptedFile = EncryptedFile.Builder(
                context,
                tempFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            return encryptedFile.openFileInput().use { it.readBytes() }
        } finally {
            tempFile.delete()
        }
    }
}
