package com.kontenery.service

import com.kontenery.data.AuthState
import com.kontenery.library.model.Contract
import com.kontenery.model.Reading
import com.kontenery.model.Payment
import com.kontenery.model.PaymentDto
import com.kontenery.model.Product
import com.kontenery.model.Submeter
import com.kontenery.model.invoice.Invoice
import com.kontenery.library.utils.errors.ErrorMessage
import com.kontenery.model.Client
import com.kontenery.model.ClientBankAccount
import com.kontenery.model.ClientOnList
import com.kontenery.model.ModalData
import com.kontenery.model.PaymentsListForFinanceTable
import com.kontenery.model.PrevYearBalance
import com.kontenery.model.TableRowFinance
import com.kontenery.model.auth.UserCredentials
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.enums.UtilityType
import com.kontenery.model.enums.now
import com.kontenery.model.invoice.InvoiceFeature
import com.kontenery.model.invoice.Position
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class ParkingAppState(
    val authState: AuthState = AuthState(),
    val userCredentials: UserCredentials? = null,

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
    val serverHealthStatus: String? = null,
    val financeYear: Int? = LocalDate.now().year,
    val prevYearsBalance: PrevYearBalance? = null,
    val invoiceFeature: InvoiceFeature = InvoiceFeature(InvoiceType.UTILITIES),
    val reading: Reading? = null,

    val confirmModal: ModalData? = null,

    val clients: List<ClientOnList> = listOf(), // mockClientsList,
    val products: List<Product> = listOf(), // mockProducts,
    val contracts: List<Contract> = listOf(),
    val invoices: List<Invoice> = listOf(),
    val payments: List<Payment> = listOf(),
    val responseErrors: List<ErrorMessage> = listOf(),
    val submeters: List<Submeter> = fakeSubmeter,  // fakeSubmeter,
    val clientsWithPayments: List<PaymentsListForFinanceTable> = listOf(),
    val financeTable: List<TableRowFinance> = listOf(),
)

val fakeReadings1 = listOf<Reading>(
    Reading(1, 1, UtilityType.ELECTRICITY, 0.00, LocalDate.now(), 1.00)
)
val fakeReadings2 = listOf<Reading>(
    Reading(2, 2, UtilityType.WATER, 0.00, LocalDate.now(), 5.00)
)
val fakeReadings3 = listOf<Reading>(
    Reading(3, 3, UtilityType.ELECTRICITY, 0.00, LocalDate.now(), 1.00),
    Reading(4, 3, UtilityType.ELECTRICITY, 10.00, LocalDate.now().plus(1, DateTimeUnit.DAY), 1.00)
)

val fakeSubmeter = listOf<Submeter>(
    Submeter(1, 7, "lokacja 1", UtilityType.ELECTRICITY, fakeReadings1, "reader2"),
    Submeter(2, 1, "lokacja 1", UtilityType.WATER, fakeReadings2, "reader2"),
    Submeter(3, 2, "lokacja 1", UtilityType.ELECTRICITY, fakeReadings3, "reader3"),
)
