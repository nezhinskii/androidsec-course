/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.data

import android.content.Context
import com.example.inventory.data.files.EncryptedFilesManager
import com.example.inventory.data.files.FileImportExportManager
import com.example.inventory.data.inventory.InventoryDatabase
import com.example.inventory.data.inventory.ItemsRepository
import com.example.inventory.data.inventory.OfflineItemsRepository
import com.example.inventory.data.settings.EncryptedPrefsManager
import com.example.inventory.data.settings.SettingsRepository
import com.example.inventory.data.settings.SettingsRepositoryImpl

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val itemsRepository: ItemsRepository
    val encryptedPrefs: EncryptedPrefsManager
    val settingsRepository: SettingsRepository
    val encryptedFiles: EncryptedFilesManager
    val fileImportExportManager: FileImportExportManager
}

/**
 * [AppContainer] implementation that provides instance of [com.example.inventory.data.inventory.OfflineItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(
            InventoryDatabase.getDatabase(context, encryptedPrefs).itemDao(),
            fileImportExportManager
        )
    }

    private val _encryptedPrefs = EncryptedPrefsManager(context)
    override val encryptedPrefs: EncryptedPrefsManager get() = _encryptedPrefs


    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(encryptedPrefs)
    }

    override val encryptedFiles: EncryptedFilesManager by lazy {
        EncryptedFilesManager(context)
    }

    override val fileImportExportManager: FileImportExportManager by lazy {
        FileImportExportManager(
            encryptedFiles = encryptedFiles,
            contentResolver = context.contentResolver
        )
    }
}
