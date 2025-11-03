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

package com.example.inventory.ui.item

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.inventory.ItemsRepository
import com.example.inventory.data.settings.AppSettings
import com.example.inventory.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    val itemDetailsState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map {
                ItemDetailsUiState(outOfStock = it.quantity <= 0, itemDetails = it.toItemDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemDetailsUiState()
            )

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    fun reduceQuantityByOne() {
        viewModelScope.launch {
            val currentItem = itemDetailsState.value.itemDetails.toItem()
            if (currentItem.quantity > 0) {
                itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - 1))
            }
        }
    }

    fun deleteItem() {
        viewModelScope.launch {
            itemsRepository.deleteItem(itemDetailsState.value.itemDetails.toItem())
        }
    }

    fun getShareableText(): String {
        val item = itemDetailsState.value.itemDetails.toItem()
        return """
        Item Details:
            ID: ${item.id}
            Name: ${item.name}
            Price: ${item.formatedPrice()}
            Quantity: ${item.quantity}
            Supplier Name: ${item.supplierName}
            Supplier Email: ${item.supplierEmail}
            Supplier Phone: ${item.supplierPhone}
        """.trimIndent()
    }

    fun exportToFile(uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val itemId = itemDetailsState.value.itemDetails.id
                itemsRepository.exportItem(itemId, uri)
                onResult(true, "File saved")
            } catch (e: Exception) {
                Log.e("ERROR",e.message.toString());
                onResult(false, e.message ?: "Save error")
            }
        }
    }
}

/**
 * UI state for ItemDetailsScreen
 */
data class ItemDetailsUiState(
    val outOfStock: Boolean = true,
    val itemDetails: ItemDetails = ItemDetails()
)
