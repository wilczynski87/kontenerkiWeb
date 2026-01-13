package com.kontenery.service

import com.kontenery.library.model.Contract
import com.kontenery.model.Payment
import com.kontenery.model.PaymentDto
import com.kontenery.model.Product
import com.kontenery.library.model.Submeter
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.model.invoice.Position
import com.kontenery.library.utils.errors.ErrorMessage
import com.kontenery.model.Client
import com.kontenery.model.ClientBankAccount
import com.kontenery.model.ClientOnList
import com.kontenery.model.ModalData
import com.kontenery.model.PaymentsListForFinanceTable
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.now
import kotlinx.datetime.LocalDate

data class ParkingAppState(
    val loggedUser: Client? = null,

    val clientNavRow: Long? = null,
    val client: Client? = null,
    val canGoBack: Boolean = false,
    val targetScreen: CurrentScreen? = null,
    val triggerScreen: CurrentScreen? = null,
    val currentScreen: CurrentScreen = CurrentScreen.CLIENTS_LIST,
    val newProduct: Product? = null,
    val productNavRow: Long? = null,
    val contractNavRow: Long? = null,
    val contract: Contract? = null,
    val clientEnabled: Boolean? = true,
    val productEnabled: Boolean? = true,
    val addNewProductError: Boolean = false,
    val clientListError: Boolean = false,
    val invoice: Invoice? = null,
    val position: Position? = null,
    val bankAccount: ClientBankAccount? = null,
    val payment: PaymentDto? = null,
    val enabledChangeClient: Boolean? = true,
    val forDate: LocalDate? = LocalDate.now(),

    val confirmModal: ModalData? = null,

    val clients: List<ClientOnList> = listOf(), // mockClientsList,
    val products: List<Product> = listOf(), // mockProducts,
    val contracts: List<Contract> = listOf(),
    val invoices: List<Invoice> = listOf(),
    val payments: List<Payment> = listOf(),
    val responseErrors: List<ErrorMessage> = listOf(),
    val submeters: List<Submeter> = listOf(),
    val clientsWithPayments: List<PaymentsListForFinanceTable> = listOf(),

)
