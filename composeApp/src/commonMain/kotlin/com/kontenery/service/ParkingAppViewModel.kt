package com.kontenery.service

import com.kontenery.controller.ApiClientsService
import com.kontenery.library.model.Contract
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.model.Client
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientOnList
import com.kontenery.model.ClientPersonalData
import com.kontenery.model.ModalData
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.endOfCurrentYear
import com.kontenery.model.enums.startOfCurrentYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class ParkingAppViewModel(
    private val viewModelScope: CoroutineScope
) {
    private val _state = MutableStateFlow(ParkingAppState())
    val state: StateFlow<ParkingAppState> = _state.asStateFlow()

    init {
        initializeUiState()
        getClientsList(0, 100)
    }

    private fun initializeUiState() {
        _state.value = ParkingAppState(clientNavRow = 1L)
    }
    /*
        MODAL
     */
    fun closeConfirmationModal() {
        _state.update { currentState ->
            currentState.copy(confirmModal = null)
        }
    }

    fun createConfirmationModal(
        modal: ModalData = ModalData(onDismissRequest = {closeConfirmationModal()})
    ) {
        _state.update { currentState ->
            currentState.copy(confirmModal = modal)
        }
    }

    fun closeResponseModal() {
        _state.update { currentState ->
            currentState.copy(responseErrors = listOf())
        }
    }

    /*
        BACK BUTTON
     */

    fun setGoBack(targetScreen: CurrentScreen, triggerScreen: CurrentScreen) {
        _state.update { currentState ->
            currentState.copy(
                canGoBack = true,
                triggerScreen = triggerScreen,
                targetScreen = targetScreen
            )
        }
    }
    fun goBack() {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = currentState.targetScreen ?: CurrentScreen.CLIENTS_LIST,
                canGoBack = false
            )
        }
    }
    fun checkGoBack() {
        val currentScreen: CurrentScreen = state.value.currentScreen
        val triggerScreen: CurrentScreen? = state.value.triggerScreen
        var canGoBack: Boolean = false
        if (currentScreen == triggerScreen) canGoBack = true
        if (triggerScreen == null) canGoBack = false

        _state.update { currentState ->
            currentState.copy(canGoBack = currentScreen == triggerScreen)
        }
    }

    fun toggleClientNavRow(clientId: Long) {
        if (clientId == state.value.clientNavRow) {
            _state.update { currentState ->
                currentState.copy(clientNavRow = null)
            }
        } else {
            _state.update { currentState ->
                currentState.copy(clientNavRow = clientId)
            }
        }
    }

    fun getClientsList(page: Int, size: Int) {
        val page: Int? = null
        val size: Int = 10
        viewModelScope.launch {
            try {
                val clientsCount: Long = ApiClientsService.clients.clientListSize()
                val pagesCount: Int = (clientsCount / size).toInt()
                _state.update { currentState ->
                    currentState.copy(clients = mutableListOf())
                }
                for(i in 0..pagesCount) {
                    val clients: List<ClientOnList> = ApiClientsService.clients.getClientList(i, size)
//                    Log.i("getClientsList", "dane: $clients")
                    _state.update { currentState ->
                        currentState.copy(
                            clients = (currentState.clients + clients).distinctBy { it.id }
                        )
                    }
                }
            } catch (e: Exception) {
                println("getClientsList nie udało się pobrać danych $e")
                _state.update { currentState ->
                    currentState.copy(clientListError = true)
                }
            }
        }
    }

    fun toggleClientsListModal() {
        _state.update { currentState ->
            currentState.copy(clientListError = !state.value.clientListError)
        }
    }

    fun toClientData(idClient: Long? = null) {
        // fech client data by Id
        viewModelScope.launch {
            try {
                val client: Client = if (idClient != null) {
                    ApiClientsService.clients.getClientData(idClient)
                } else Client(null, ClientPersonalData(), ClientCompanyData(), true)
                // update state
                _state.update { currentState ->
                    currentState.copy(
                        client = client,
                        currentScreen = CurrentScreen.CLIENT_DATA
                    )
                }
//                Log.i("toClientData", "dane klienta: $client")
                println("toClientData dane klienta: $client")
            } catch (e: Exception) {
//                Log.i("Złe dane", "nie udało się pobrać danych $e")
                println("Złe dane nie udało się pobrać danych $e")
            }
        }
    }

    fun updateClient(client: Client) {
        _state.update { currentState ->
            currentState.copy(client = client)
        }
    }

    fun updateClient(clientId: Long?) {
        viewModelScope.launch {
            try {
                val client: Client = ApiClientsService.clients.getClientData(clientId!!)

                // fetch client data by Id
                _state.update { currentState ->
                    currentState.copy(client = client)
                }
            } catch (e: Exception) {
                println("updateClientError $e")
            }
        }
    }

    fun fetchPaymentsForClient(
        clientId: Long,
        from: LocalDate? = LocalDate.startOfCurrentYear(),
        to: LocalDate? = LocalDate.endOfCurrentYear()
    ) {
        viewModelScope.launch {
            try {
//                Log.i("fetchForClientPayments:", "clientId: $clientId, from: $from, to: $to")
                val payments = ApiClientsService.payments.getPaymentsForClient(
                    clientId,
                    from.toString(),
                    to.toString()
                )
                println("fetchForClientPayments dane: $payments")
                _state.update { currentState ->
                    currentState.copy(payments = payments)
                }
            } catch (e: Exception) {
                println("fetchForClientPayments Can not tech invoices: $e")
            }
        }
    }

    fun fetchInvoicesForClient(clientId: Long, from: LocalDate? = LocalDate.startOfCurrentYear(), to: LocalDate? = LocalDate.endOfCurrentYear()) {
        viewModelScope.launch {
            try {
//                Log.i("fetchForClientInvoices", "from: $from, to: $to, clientId: $clientId")
                val invoices: List<Invoice> =
                    ApiClientsService.invoices.fetchInvoicesForClient(
                        clientId,
                        from.toString(),
                        to.toString(),
                    )
                println("fetchForClientInvoices: dane: $invoices")
                _state.update { currentState ->
                    currentState.copy(
                        invoices = invoices
                    )
                }
            } catch (e: Exception) {
                println("fetchInvoicesForClient nie udało się pobrać danych $e")
            }
        }
    }

    fun toContractList() {
        // fech client list
        // update list
        _state.update { currentState ->
            currentState.copy(
//                clients = clients,
                currentScreen = CurrentScreen.CLIENT_CONTRACTS
            )
        }
    }

    fun fetchContractsForClient(clientId: Long) {
        viewModelScope.launch {
            try {
                val contracts: List<Contract> =
                    ApiClientsService.contracts.getContractsByClient(clientId)

                _state.update { currentState ->
                    currentState.copy(contracts = contracts)
                }
            } catch (e: Exception) {
                println("fetchContractsForClient nie udało się pobrać danych $e")
            }
        }
    }

    fun sendPeriodicInvoice(clientId: Long) {
//        Log.i("sendPeriodicInvoice", clientId.toString())
        viewModelScope.launch {
            try {
                val response = ApiClientsService.invoices.postPeriodicInvoice(
                    clientId)
                println("sendPeriodicInvoice errors: $response")
                _state.update { currentState ->
                    currentState.copy(responseErrors = response)
                }

            } catch (e: Exception) {
                println("sendPeriodicInvoice nie udało się wysłać faktury $e")
            }
        }
    }

    fun toPaymentsMenu() {
        _state.update { currentState ->
            currentState.copy(currentScreen = CurrentScreen.PAYMENT_MENU)
        }
    }

    fun dispose() {
        viewModelScope.cancel()
    }
}
