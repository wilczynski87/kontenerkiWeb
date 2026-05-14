package com.kontenery.service

import com.kontenery.model.invoice.Subject.Seller
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.enums.now
import com.kontenery.model.invoice.Position
import com.kontenery.util.to2Decimals
import com.kontenery.util.toDoublePl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

internal class InvoiceDraftController(
    private val state: MutableStateFlow<ParkingAppState>
) {
    fun createNewInvoice(invoiceType: InvoiceType? = null) {
        val invoice = Invoice(
            invoiceNumber = null,
            invoiceTitle = null,
            invoiceDate = LocalDate.now(),
            seller = null,
            customer = null,
            products = mutableListOf(),
            vatAmountSum = null,
            priceSum = null,
            priceWithVatSum = null,
            paymentDay = null,
            invoiceSendToClient = null,
            type = invoiceType?.name,
            vatApply = false
        )

        state.update { currentState ->
            currentState.copy(invoice = invoice)
        }
    }

    fun updateInvoice(invoice: Invoice) {
        state.update { currentState ->
            currentState.copy(invoice = invoice)
        }
    }

    fun sellerForInvoiceUpdate() {
        val invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: sellerForInvoiceUpdate")
        val needInvoice = state.value.client?.needInvoice() == true

        val updatedInvoice = if (needInvoice) {
            invoice.copy(
                seller = Seller.company(null),
                invoiceTitle = "Faktura VAT",
                mainAccount = Seller.company(null).account
            )
        } else {
            invoice.copy(
                seller = Seller.personal(null),
                invoiceTitle = "Faktura imienna bez VAT",
                mainAccount = Seller.personal(null).account
            )
        }

        updateInvoice(updatedInvoice)
    }

    fun addProductToInvoice(position: Position? = null) {
        val invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: addProductToInvoice")
        val newPosition = position
            ?: state.value.position
            ?: throw NullPointerException("NewProduct is null, for: addProductToInvoice")

        val positions = invoice.products + newPosition
        val sumPrice = positions.sumOf { it.price?.toDoublePl() ?: 0.0 }
        val sumVat = positions.sumOf { it.vatAmount?.toDoublePl() ?: 0.0 }
        val sumWithVat = positions.sumOf { it.priceWithVat?.toDoublePl() ?: 0.0 }

        state.update { currentState ->
            currentState.copy(
                invoice = invoice.copy(
                    products = positions,
                    vatAmountSum = sumVat.toString(),
                    priceSum = sumPrice.toString(),
                    priceWithVatSum = sumWithVat.toString(),
                )
            )
        }
    }

    fun removeProductFromInvoice(index: Int) {
        val invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: removeProductFromInvoice")
        val newProducts = invoice.products.toMutableList()
        newProducts.removeAt(index)

        state.update { currentState ->
            currentState.copy(invoice = invoice.copy(products = newProducts))
        }
    }

    fun updatePosition(position: Position?) {
        val updatedPosition = position ?: Position(
            vatRate = "23",
            productName = null,
            unitPrice = null,
            quantity = null,
            price = null,
            vatAmount = null,
            priceWithVat = null,
        )

        state.update { currentState ->
            currentState.copy(position = updatedPosition)
        }
    }

    fun calculatePosition(position: Position) {
        if (position.unitPrice.isNullOrBlank() || position.quantity.isNullOrBlank()) {
            updatePosition(position)
            return
        }

        val newPrice = position.unitPrice.toDoubleOrNull()?.times(position.quantity.toDouble()) ?: 0.00
        val newVatAmount = if (position.vatRate.isNullOrBlank().not()) {
            newPrice * position.vatRate.toDouble() / 100
        } else {
            0.00
        }
        val newPriceWithVat = newPrice + newVatAmount

        updatePosition(
            Position(
                vatRate = position.vatRate,
                productName = position.productName,
                unitPrice = position.unitPrice,
                quantity = position.quantity,
                price = newPrice.to2Decimals(),
                vatAmount = newVatAmount.to2Decimals(),
                priceWithVat = newPriceWithVat.to2Decimals(),
            )
        )
    }

    fun onUnitPriceChanged(input: String) {
        updatePosition(state.value.position?.copy(unitPrice = input))
    }
}
