package com.example.inventory.ui.item

object ItemValidation {
    private val emailRegex = "^[A-Za-z0-9.-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9.-]+$".toRegex()
    private val phoneRegex = "^\\+?[1-9][0-9]{7,14}$".toRegex()
    fun validateInput(itemDetails: ItemDetails): Boolean {
        return isValidName(itemDetails.name) &&
                isValidPrice(itemDetails.price) &&
                isValidQuantity(itemDetails.quantity) &&
                isValidSupplierName(itemDetails.supplierName) &&
                isValidSupplierEmail(itemDetails.supplierEmail) &&
                isValidSupplierPhone(itemDetails.supplierPhone)
    }

    fun isValidName(name: String): Boolean = name.isNotBlank()

    fun isValidPrice(price: String): Boolean = price.isNotBlank() && price.toDoubleOrNull() != null

    fun isValidQuantity(quantity: String): Boolean = quantity.isNotBlank() && quantity.toIntOrNull() != null

    fun isValidSupplierName(supplierName: String): Boolean = supplierName.isNotBlank()

    fun isValidSupplierEmail(email: String): Boolean = email.isNotBlank() && email.matches(emailRegex)

    fun isValidSupplierPhone(phone: String): Boolean = phone.isNotBlank() && phone.matches(phoneRegex)

    fun areAllFieldsFilled(itemDetails: ItemDetails): Boolean {
        return with(itemDetails) {
            name.isNotBlank() &&
            price.isNotBlank() &&
            quantity.isNotBlank() &&
            supplierName.isNotBlank() &&
            supplierEmail.isNotBlank() &&
            supplierPhone.isNotBlank()
        }
    }
}