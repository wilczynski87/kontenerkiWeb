package com.kontenery.service

import com.kontenery.model.Payment
import com.kontenery.model.Product
import com.kontenery.model.enums.now
import com.kontenery.model.invoice.Invoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

internal class ParkingSelectionController(
    private val state: MutableStateFlow<ParkingAppState>
) {
    fun toggleClientNavRow(clientId: Long) {
        state.update { currentState ->
            currentState.copy(
                clientNavRow = if (clientId == currentState.clientNavRow) null else clientId
            )
        }
    }

    fun toggleClientsListModal() {
        state.update { currentState ->
            currentState.copy(clientListError = !currentState.clientListError)
        }
    }

    fun getClientNameById(clientId: Long): String? {
        return state.value.clients.find { it.id == clientId }?.name
    }

    fun newProduct(product: Product?) {
        state.update { currentState ->
            currentState.copy(newProduct = product)
        }
    }

    fun updateProduct(product: Product) {
        state.update { currentState ->
            currentState.copy(newProduct = product)
        }
    }

    fun clearProduct() {
        state.update { currentState ->
            currentState.copy(newProduct = null)
        }
    }

    fun toggleProductNavRow(productId: Long?) {
        state.update { currentState ->
            currentState.copy(
                productNavRow = if (productId == currentState.productNavRow) null else productId
            )
        }
    }

    fun getClientOverdue(clientId: Long): Double? {
        return state.value.clients.find { it.id == clientId }?.paymentsOverdue
    }

    fun updateInvoicesAndPayments(invoices: List<Invoice>, payments: List<Payment>) {
        state.update { currentState ->
            currentState.copy(invoices = invoices, payments = payments)
        }
    }

    fun updateForDate(date: LocalDate? = LocalDate.now()) {
        state.update { currentState ->
            currentState.copy(forDate = date)
        }
    }

    fun changeFinanceYear(year: Int) {
        state.update { currentState ->
            currentState.copy(financeYear = year)
        }
    }
}
