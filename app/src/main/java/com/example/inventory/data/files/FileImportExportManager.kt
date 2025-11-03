package com.example.inventory.data.files

import android.content.ContentResolver
import android.net.Uri
import com.example.inventory.data.inventory.Item
import com.example.inventory.data.inventory.ItemJson
import com.example.inventory.data.inventory.Source
import kotlinx.serialization.json.Json

class FileImportExportManager(
    private val encryptedFiles: EncryptedFilesManager,
    private val contentResolver: ContentResolver
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun exportItem(item: Item, uri: Uri) {
        val itemJson = ItemJson(
            name = item.name,
            price = item.price,
            quantity = item.quantity,
            supplierName = item.supplierName,
            supplierEmail = item.supplierEmail,
            supplierPhone = item.supplierPhone
        )

        val jsonString = json.encodeToString(itemJson)
        val plainBytes = jsonString.toByteArray(Charsets.UTF_8)

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            encryptedFiles.encryptToStream(plainBytes, outputStream)
        } ?: throw IllegalStateException("Cannot open OutputStream")
    }

    fun importItem(uri: Uri): Item {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open InputStream")

        return inputStream.use { stream ->
            val encryptedBytes = encryptedFiles.decryptFromStream(stream)
            val jsonString = encryptedBytes.toString(Charsets.UTF_8)
            val itemJson = json.decodeFromString<ItemJson>(jsonString)
            Item(
                name = itemJson.name,
                price = itemJson.price,
                quantity = itemJson.quantity,
                supplierName = itemJson.supplierName,
                supplierEmail = itemJson.supplierEmail,
                supplierPhone = itemJson.supplierPhone,
                source = Source.FILE
            )
        }
    }
}