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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.inventory.Item
import com.example.inventory.data.inventory.ItemsRepository
import com.example.inventory.data.inventory.Source
import com.example.inventory.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(
    private val itemsRepository: ItemsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()

            if (settings.useDefaultQuantity) {
                val defaultQuantity = settings.defaultQuantity.coerceAtLeast(0).toString()
                updateUiState(
                    itemUiState.itemDetails.copy(quantity = defaultQuantity)
                )
            }
        }
    }

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            isEntryValid = ItemValidation.areAllFieldsFilled(itemDetails))
    }

    fun saveItem(): Boolean {
        if (ItemValidation.validateInput(itemUiState.itemDetails)) {
            viewModelScope.launch {
                itemsRepository.insertItem(itemUiState.itemDetails.toItem())
            }
            return true
        } else {
            return false
        }
    }

    fun importFromFile(uri: Uri, onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val item = itemsRepository.importFromFile(uri)
                onComplete()
            } catch (e: Exception) {
                Log.e("ERROR", e.message.toString())
                onError(e.message ?: "Import error")
            }
        }
    }
}

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val quantity: String = "",
    val supplierName: String = "",
    val supplierEmail: String = "",
    val supplierPhone: String = "",
    val source: Source = Source.MANUAL
)

/**
 * Extension function to convert [ItemDetails] to [Item]. If the value of [ItemDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [ItemDetails.quantity] is not a valid [Int], then the quantity will be set to 0
 */
fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0,
    supplierName = supplierName,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone,
    source = source
)

fun Item.formatedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity.toString(),
    supplierName = supplierName,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone,
    source = source
)
