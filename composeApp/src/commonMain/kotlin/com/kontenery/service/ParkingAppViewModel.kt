package com.kontenery.service

import com.kontenery.controller.ApiClientsService
import com.kontenery.library.model.Contract
import com.kontenery.library.model.Deposit
import com.kontenery.model.Payment
import com.kontenery.model.PaymentDto
import com.kontenery.model.Product
import com.kontenery.model.Product.Container
import com.kontenery.model.Product.Yard
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.model.invoice.Position
import com.kontenery.library.model.invoice.Subject
import com.kontenery.library.model.invoice.Subject.Seller
import com.kontenery.library.utils.InvoiceType
import com.kontenery.model.Client
import com.kontenery.model.ClientBankAccount
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientOnList
import com.kontenery.model.ClientPersonalData
import com.kontenery.model.ModalData
import com.kontenery.model.PaymentForFinanceTable
import com.kontenery.model.PaymentsListForFinanceTable
import com.kontenery.model.TableRowFinance
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.endOfCurrentYear
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.text.toDouble

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
//                    println("getClientsList", "dane: $clients")
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
//                println("toClientData", "dane klienta: $client")
                println("toClientData dane klienta: $client")
            } catch (e: Exception) {
//                println("Złe dane", "nie udało się pobrać danych $e")
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
//                println("fetchForClientPayments:", "clientId: $clientId, from: $from, to: $to")
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
                println("fetchForClientInvoices from: $from, to: $to, clientId: $clientId")
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

    fun dispose() {
        viewModelScope.cancel()
    }


    fun updateClientPersonalData(clientPrivate: ClientPersonalData) {
        val client: Client = state.value.client ?: Client()
        _state.update { currentState ->
            currentState.copy(client = client.copy(clientPrivate = clientPrivate))
        }
    }

    fun updateClientCompanyData(clientCompany: ClientCompanyData) {
        _state.update { currentState ->
            currentState.copy(client = state.value.client?.copy(clientCompany = clientCompany))
        }
    }

    fun createNewClient() {
        val newClient = Client(null, ClientPersonalData(), ClientCompanyData(), true)
        _state.update { currentState ->
            currentState.copy(client = newClient)
        }
    }

    fun isActiveClientToggle() {
        val client: Client = state.value.client ?: Client(isActive = true)
        if(client.isActive == null) {
            _state.update { currentState ->
                currentState.copy(client = client.copy(isActive = true))
            }
        } else {
            _state.update { currentState ->
                currentState.copy(client = client.copy(isActive = (client.isActive.not())))
            }
        }
    }

    fun needInvoiceToggle() {
        val client: Client = state.value.client ?: return
        val clientCompanyData: ClientCompanyData = client.clientCompany ?: return
        val needInvoice: Boolean = clientCompanyData.needInvoice ?: false
        _state.update { currentState ->
            currentState.copy(client = client.copy(clientCompany = clientCompanyData.copy(needInvoice = needInvoice.not())))
        }

    }

    fun putClient() {
        // save client to DB (update)
        viewModelScope.launch {
            try {
                val stateClient: Client? = state.value.client

                if (stateClient != null && stateClient.id != null) {
                    val client: Client = ApiClientsService.clients.updateClient(
                        stateClient.id,
                        state.value.client!!
                    )

                    println("putClient zaktualizowano klienta: $client")

                    _state.update { currentState ->
                        currentState.copy(client = client)
                    }
                } else println("putClient nie udało się zaktualizować klienta: $stateClient")

            } catch (e: Exception) {
                println("putClient nie udało się zaktualizować danych klienta, błąd: $e")
            }
        }
    }

    fun saveClient(client: Client) {
        var client: Client = client
        viewModelScope.launch {
            try {
                val savedClient: Client = ApiClientsService.clients.saveClient(client)
                println("saveClient zapisano klienta: $savedClient")
                _state.update { currentState ->
                    currentState.copy(client = savedClient)
                }
            } catch (e: Exception) {
                println("saveClient nie udało się zapisać danych: $client,\n $e")
            }
        }
        // save client to DB
    }

    fun fetchClientForContract(clientId: Long){
        viewModelScope.launch {
            try {
                val client: Client? = ApiClientsService.clients.getClientData(clientId)
                println("fetchClientForContract  $client")
                _state.update { currentState ->
                    currentState.copy(contract = currentState.contract?.copy(client = client))
                }
            } catch (e: Exception) {
                println("fetchClientById nie udało się odnaleźć danych, o id: $clientId,\n $e")
            }
        }
    }

    /*
        Product methods
    */
    fun getProductsList(page: Int = 0, size: Int = 100) {
        viewModelScope.launch {
            try {
                val products: List<Product> =
                    ApiClientsService.products.getProductList(page, size)

                _state.update { currentState ->
                    currentState.copy(products = products)
                }

            } catch (e: Exception) {
                println("getProductsList Error nie udało się pobrać danych $e")
            }
        }
    }

    fun saveProduct(product: Product) {
        println("sendProductToServer zapisuje produkt: $product")
        viewModelScope.launch {
            try {
                val savedProduct = if (product.id == null) postToServer(product)
                else putToServer(product)

//                    when(product) {
//                    is Yard -> ApiClientsService.retrofitProductService.saveYard(product)
//                    is Container -> ApiClientsService.retrofitProductService.saveContainer(product)
//                    else -> throw TypeNotPresentException("Product Error", Exception("Nie mogę dopasować produktu do typu"))
//                }
                println("saveProduct zapisano produkt: $savedProduct")
                _state.update { currentState ->
                    currentState.copy(newProduct = null)
                }
            } catch (e: Exception) {
                println("saveProduct nie udało się zapisać danych: $e")
                _state.update { currentState ->
                    currentState.copy(addNewProductError = true)
                }
            }
        }
    }

    private suspend fun postToServer(product: Product): Product {
        return when (product) {
            is Yard -> ApiClientsService.products.saveYard(product)
            is Container -> ApiClientsService.products.saveContainer(product)
        }
    }

    private suspend fun putToServer(product: Product): Product {
        val id: Long = product.id ?: throw NullPointerException("Product Error, brak ID")
        return when (product) {
            is Yard -> ApiClientsService.products.updateYard(id, product)
            is Container -> ApiClientsService.products.updateContainer(id, product)
        }
    }

    fun newProduct(product: Product?) {
        _state.update { currentState ->
            currentState.copy(newProduct = product)
        }
    }

    fun updateProduct(product: Product) {
        _state.update { currentState ->
            currentState.copy(newProduct = product)
        }
    }

    fun clearProduct() {
        _state.update { currentState ->
            currentState.copy(newProduct = null)
        }
    }

    fun toggleProductNavRow(productId: Long?) {
        if (productId == state.value.productNavRow) {
            _state.update { currentState ->
                currentState.copy(productNavRow = null)
            }
        } else {
            _state.update { currentState ->
                currentState.copy(productNavRow = productId)
            }
        }
    }


    fun getClientOverdue(clientId: Long): Double? {
        var client: ClientOnList? = state.value.clients.find { it.id == clientId }
        if (client == null) {
            getClientsList(0, 1000)
            val widerListClient = state.value.clients.find { it.id == clientId }
            if(widerListClient == null) return null
            else client = widerListClient
        }
        return client.paymentsOverdue
    }

    /*
        Navigation methods:
     */

    fun toClientList() {
        // fech client data
        getClientsList(0, 100)
        // update state
        _state.update { currentState ->
            currentState.copy(
//                clients = clients,
                currentScreen = CurrentScreen.CLIENTS_LIST
            )
        }
    }

    /*
        TO PRODUCKT:
    */
    fun toProductsList() {
        // fech client data
        getProductsList(0, 100)
        // update state
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.PRODUCTS_LIST
            )
        }
    }

    fun toAddProduct() {
        // fech client list
        // update list
        _state.update { currentState ->
            currentState.copy(
//                clients = clients,
                currentScreen = CurrentScreen.ADD_PRODUCT
            )
        }
    }

    /*
        TO CONTRACT:
     */
    fun toContractMenu(
        contractId: Long? = null,
        productEnabled: Boolean = true,
        clientEnabled: Boolean = true,
    ) {
        if (contractId == null) {
            println("toContractMenu New")
            updateContract(Contract())
            toAddContract(productEnabled, clientEnabled)
        } else {
            println("toContractMenu toUpdateContract, with Id: $contractId")
            fetchContractById(contractId)
            toAddContract(productEnabled, clientEnabled)
        }
    }

    fun toAddContract(
        productEnabled: Boolean = true,
        clientEnabled: Boolean = true,
    ) {
        // update list
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.ADD_CONTRACT,
                productEnabled = productEnabled,
                clientEnabled = clientEnabled,
            )
        }
    }

    fun getContract(): Contract? {
        return state.value.contract
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

    fun fetchContractById(contractId: Long) {
        viewModelScope.launch {
            try {
                val contract: Contract =
                    ApiClientsService.contracts.getContractById(contractId)
                        ?: throw NullPointerException("Can not find contract with id: $contractId")

                updateContract(contract)

            } catch (e: Exception) {
                println("fetchContractsForClient nie udało się pobrać danych $e")
            }
        }
    }

    fun fetchContractByProductId(productId: Long) {
        viewModelScope.launch {
            try {
                val contract: Contract =
                    ApiClientsService.contracts.getContractByProductId(productId)
                        ?: throw NullPointerException("Can not find contract with id: $productId")
                println("fetchContractByProductId $contract")

                updateContract(contract)

            } catch (e: Exception) {
                println("fetchContractsForClient nie udało się pobrać danych $e")
            }
        }
    }

    fun updateContract(contract: Contract) {
        try {
            println("updateContract $contract")
            _state.update { currentState ->
                currentState.copy(contract = contract)
            }
        } catch (e: Exception) {
            println("updateContract nie udało się zapisać danych $e")
        }
    }

    fun getContractByProductId(productId: Long) {
        viewModelScope.launch {
            try {
                val contract: Contract =
                    ApiClientsService.contracts.getContractByProductId(productId)
                        ?: throw NullPointerException("Can not find contract with id: $productId")
                println("fetchContractByProductId $contract")

                updateContract(contract)

                toContractMenu(contract.id, productEnabled = false, clientEnabled = true)

            } catch (e: Exception) {
                println("fetchContractsForClient nie udało się pobrać danych $e")
            }
        }
    }

    // TODO POPRAWIć - daje 400
    fun saveContractToDB(contract: Contract) {
        // save contract to DB
        viewModelScope.launch {
            try {
//                println("saveContractToDB Przed zapisem: $contract")
                println("saveContractToDB rzed zapisem: ${contract.toContractDTO()}")
                val contract: Contract =
                    ApiClientsService.contracts.postContract(contract.toContractDTO())
//                println("saveContractToDB zapisano umowę: $contract")

                _state.update { currentState ->
                    currentState.copy(contract = contract)
                }
            } catch (e: Exception) {
                println("saveContractToDB nie udało się pobrać danych $e")
            }
        }
    }

    fun putContractToDB(contract: Contract) {
        // update contract to DB
        viewModelScope.launch {
            try {
                println("putContractToDB Przed zapisem: $contract")
                val contract: Contract = ApiClientsService.contracts.putContract(
                    contract.id!!,
                    contract.toContractDTO()
                )
                println("putContractToDB zapisano umowę: $contract")

                updateContract(contract)
            } catch (e: Exception) {
                println("saveContractToDB nie udało się pobrać danych $e")
            }
        }
    }

    fun deleteContract(id: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClientsService.contracts.deleteContract(id)
                //            if(response.not()) uruchomić modal z błędem lub sukcesem
            } catch (e: Exception) {
                println("saveContractToDB nie udało się usunąć danych $e")
            }
        }
    }

    /*
        DEPOSIT
     */
    fun depositChange(newDeposit: Deposit) {
        var contract: Contract? = state.value.contract ?: return
        contract = contract?.copy(deposit = newDeposit)
        _state.update { currentState ->
            currentState.copy(contract = contract)
        }
    }

    fun toggleAddProductModal() {
        _state.update { currentState ->
            currentState.copy(addNewProductError = !state.value.addNewProductError)
        }
    }

    /*
        TO INVOICE:
    */
    fun sendPeriodicInvoice(clientId: Long) {
//        println("sendPeriodicInvoice", clientId.toString())
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

    // TODO obsługa odpowiedzi do napisania
    fun postPeriodicInvoiceAgain(invoiceNumber: String) {
        viewModelScope.launch {
            try {
                val response = ApiClientsService.invoices.postPeriodicInvoiceAgain(invoiceNumber)
                println("postPeriodicInvoiceAgain resp for: $invoiceNumber:\n $response")
//                _state.update { currentState ->
//                    currentState.copy(responseErrors = response)
//                }
            } catch (e: Exception) {
                println("postPeriodicInvoiceAgain nie udało się wysłać faktury nr: $invoiceNumber ponownie $e")
            }
        }
    }

    fun sendPeriodicInvoiceToAllClients(period: LocalDate = LocalDate.now()) {
        println("sendPeriodicInvoiceToAllClients period: $period")
        viewModelScope.launch {
            try {
                val response = ApiClientsService.invoices.postPeriodicInvoiceToAllClients(period.toString())
                println("sendPeriodicInvoiceForAll errors: $response")
                _state.update { currentState ->
                    currentState.copy(responseErrors = response)
                }
            } catch (e: Exception) {
                println("sendPeriodicInvoiceToAllClients nie udało się wysłać faktur $e")
            }
        }
    }

    // TODO do wykasowania!!!
    fun updateInvoicesAndPayments(invoices: List<Invoice>, payments: List<Payment>) {
        _state.update { currentState ->
            currentState.copy(invoices = invoices, payments = payments)
        }
    }

    fun toAddInvoice() {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.ADD_INVOICE
            )
        }
    }

    fun toUploadPayments() {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.UPLOAD_PAYMENTS
            )
        }
    }

    fun createNewInvoice(invoiceType: InvoiceType? = null) {
        val positions = mutableListOf<Position>()
        val invoice = Invoice(
            invoiceNumber = null,
            invoiceTitle = null,
            invoiceDate = LocalDate.now(),
            seller = null,
            customer = null,
            products = positions,
            vatAmountSum = null,
            priceSum = null,
            priceWithVatSum = null,
            paymentDay = null,
            invoiceSendToClient = null,
            type = invoiceType?.name,
            vatApply = false
        )

        _state.update { currentState ->
            currentState.copy(
                invoice = invoice
            )
        }
    }

    fun updateInvoice(invoice: Invoice) {
        _state.update { currentState ->
            currentState.copy(invoice = invoice)
        }
    }

    fun sellerForInvoiceUpdate() {
        val invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: sellerForInvoiceUpdate")
        val needInvoice: Boolean = state.value.client?.needInvoice() == true
        if(needInvoice) updateInvoice(invoice.copy(
            seller = Seller.company(null),
            invoiceTitle = "Faktura VAT",
            mainAccount = Seller.company(null).account
        )
        ) else updateInvoice(invoice.copy(
            seller = Seller.personal(null),
            invoiceTitle = "Faktura imienna bez VAT",
            mainAccount = Seller.personal(null).account
        )
        )
    }

    fun addProductToInvoice() {
        val invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: addProductToInvoice")
        val newPosition: Position = state.value.position ?: throw NullPointerException("NewProduct is null, for: addProductToInvoice")

        val positions: List<Position> = state.value.invoice?.products?.plus(newPosition) ?: listOf(newPosition)
        val sumPrice = positions.sumOf { it.price?.toDoublePl() ?: 0.0 }
        val sumVat = positions.sumOf { it.vatAmount?.toDoublePl() ?: 0.0 }
        val sumWithVat = positions.sumOf { it.priceWithVat?.toDoublePl() ?: 0.0 }

        val updatedInvoice = invoice.copy(
            products = positions,
            vatAmountSum = sumVat.toString(),
            priceSum = sumPrice.toString(),
            priceWithVatSum = sumWithVat.toString(),
        )

        _state.update { currentState ->
            currentState.copy(
                invoice = updatedInvoice
            )
        }
    }
    fun sumSum() {
        val positions: List<Position> = state.value.invoice?.products ?: listOf()
        val sumPrice = positions.sumOf { it.price?.toDoubleOrNull() ?: 0.0 }
        val sumVat = positions.sumOf { it.vatAmount?.toDoubleOrNull() ?: 0.0 }
        val sumWithVat = positions.sumOf { it.priceWithVat?.toDoubleOrNull() ?: 0.0 }
    }

    fun updateProductToInvoice(position: Position) {
        var invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: updateProductToInvoice")

        var newProducts = invoice.products

        println("products old: " + invoice.products.toString())
        println("products new: $newProducts")

        _state.update { currentState ->
            currentState.copy(
                invoice = invoice.copy(products = newProducts.plus(position)),
            )
        }
    }

    fun removeProductFromInvoice(index: Int) {
        var invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: removeProductFromInvoice")
        var newProducts: MutableList<Position> = invoice.products.toMutableList()
        newProducts.removeAt(index)
        _state.update { currentState ->
            currentState.copy(invoice = invoice.copy(products = newProducts))
        }
    }

    fun updatePosition(position: Position?) {
        val position = position ?: Position(
            vatRate = "23",
            productName = null,
            unitPrice = null,
            quantity = null,
            price = null,
            vatAmount = null,
            priceWithVat = null,
        )
        _state.update { currentState ->
            currentState.copy(position = position)
        }
    }

    fun calculatePosition(position: Position) {
        if(position.unitPrice.isNullOrBlank() || position.quantity.isNullOrBlank()) {
            updatePosition(position)
            return
        }

        val newPrice: Double = position.unitPrice.toDoubleOrNull()?.times(position.quantity.toDouble()) ?: 0.00

        val newVatAmount: Double = if(position.vatRate.isNullOrBlank().not())
            (newPrice * position.vatRate.toDouble() / 100) else 0.00

        val newPriceWithVat: Double = (newPrice + newVatAmount)

        val position = Position(
            vatRate = position.vatRate,
            productName = position.productName,
            unitPrice = position.unitPrice,
            quantity = position.quantity,
            price = newPrice.to2Decimals(),
            vatAmount = newVatAmount.to2Decimals(),
            priceWithVat = newPriceWithVat.to2Decimals(),
        )

        updatePosition(position)
    }

    fun postCustomInvoice(clientId: Long, invoice: Invoice) {
        // save invoice to DB
        viewModelScope.launch {
            try {
//                val invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: postCustomInvoice")
                println("saveCustomInvoiceToDB $invoice")
//                var invoiceNew = invoice
//                var inv = gson.toJson(invoiceNew)
//                println("saveCustomInvoiceToDB json: $inv")
                ApiClientsService.invoices.postCustomInvoice(clientId, invoice)
                // TODO dać info o zapisanej fakturze
            } catch (e: Exception) {
                println("saveCustomInvoiceToDB nie udało się wysłać faktury $e")
            }
        }
    }

    fun updateCustomerToInvoice(clientId: Long) {
        val invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: updateCustomerToInvoice")
        viewModelScope.launch {
            try {
                val client: Client = ApiClientsService.clients.getClientData(clientId)
                val needInvoice: Boolean = client.needInvoice()

                // update invoice
                _state.update { currentState ->
                    currentState.copy(
                        invoice = invoice.copy(
                            customer = Subject.Customer.toCustomer(client),
                            seller = if(needInvoice) Seller.company(null) else Seller.personal(null),
                            mainAccount = if(needInvoice) Seller.company(null).account else Seller.personal(null).account,
                            vatApply = needInvoice
                        ),
                        client = client
                    )
                }

                // fetch client data by Id
//                _state.update { currentState ->
//                    currentState.copy(client = client)
//                }
            } catch (e: Exception) {
                println("updateClientError ${e.toString()}")
            }
        }
    }

    /*
        PAYMENTS:
     */
    fun toPaymentsMenu() {
        _state.update { currentState ->
            currentState.copy(currentScreen = CurrentScreen.PAYMENT_MENU)
        }
    }

    fun toPaymentForm(enabled: Boolean? = true) {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.PAYMENT_FORM,
                enabledChangeClient = enabled ?: true
            )
        }
    }

    fun updatePaymentState(payment: PaymentDto?) {
        _state.update { currentState ->
            currentState.copy(payment = payment)
        }
    }
    fun newPaymentState(clientId: Long? = null) {
        _state.update { currentState ->
            currentState.copy(payment = PaymentDto(
                null, 0.00, LocalDate.now(), clientId, null, null, null, null, mutableListOf()))
        }
    }
    fun fetchClientForPayment(clientId: Long) {
        viewModelScope.launch {
            try {
                val client: Client? = ApiClientsService.clients.getClientData(clientId)
                println("fetchClientForPayment $client")
                _state.update { currentState ->
                    currentState.copy(
                        client = client,
                        payment = currentState.payment?.copy(fromClientId = client?.id))
                }
            } catch (e: Exception) {
                println("fetchClientById nie udało się odnaleźć danych, o id: $clientId,\n $e")
            }
        }
    }

    fun postPaymentToDB(payment: PaymentDto) {
        viewModelScope.launch {
            try {
                println("postPayment $payment")
                val paymentSaved = ApiClientsService.payments.postPayment(payment)
            } catch (e: Exception) {
                println("postPayment nie udało się zapisać płatności,\n $e")
            }
        }
    }
    fun postPaymentToApiWithResponse(payment: PaymentDto) {
        viewModelScope.launch {
            try {
                println("postPayment $payment")
                val paymentSaved = ApiClientsService.payments.postPayment(payment)
                if(paymentSaved?.fromClient?.id != null) {
                    fetchPaymentsForClient(paymentSaved.fromClient.id)
                } else println("Probem z płatnością: $paymentSaved")
            } catch (e: Exception) {
                println("postPayment nie udało się zapisać płatności,\n $e")
            }
        }
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            try {
                println("deletePayment $paymentId")
                val paymentDeleted = ApiClientsService.payments.deletePayment(paymentId.toLong())
                // TODO modal z deleted payment
            } catch (e: Exception) {
                println("deletePayment nie udało się usunąć płatności,\n $e")
            }
        }
    }
    fun deletePaymentAndRefreshClient(paymentId: String, clientId: Long?) {
        viewModelScope.launch {
            try {
                println("deletePayment $paymentId")
                val paymentDeleted = ApiClientsService.payments.deletePayment(paymentId.toLong())
                if(paymentDeleted && clientId != null) {
                    fetchPaymentsForClient(clientId)
                    toPaymentsMenu()
                }
                // TODO modal z deleted payment
            } catch (e: Exception) {
                println("deletePayment nie udało się usunąć płatności,\n $e")
            }
        }
    }

    /*
        BANK ACCOUNT
     */

    fun toBankAccountMenu() {
        _state.update { currentState ->
            currentState.copy(currentScreen = CurrentScreen.BANK_ACCOUNT_MENU)
        }
    }

    fun newEmptyBankAccount() {
        _state.update { currentState ->
            currentState.copy(bankAccount = ClientBankAccount())
        }
    }

    fun updateBankAccount(bankAccount: ClientBankAccount? = null) {
        viewModelScope.launch {
            val client: Client? = state.value.client
            val currentDate: LocalDate = LocalDate.now()
            val oldBankAccount: ClientBankAccount = state.value.bankAccount ?: ClientBankAccount(client = client, createdAt = currentDate)
            try {
                val updatedBankAccount = if(bankAccount == null) {
                    ClientBankAccount(client = client, createdAt = currentDate)
                } else {
                    ClientBankAccount(
                        id = bankAccount.id ?: oldBankAccount.id,
                        bankAccount = bankAccount.bankAccount ?: oldBankAccount.bankAccount,
                        client = bankAccount.client ?: oldBankAccount.client,
                        createdAt = bankAccount.createdAt ?: oldBankAccount.createdAt,
                    )
                }
                _state.update { currentState ->
                    currentState.copy(bankAccount = updatedBankAccount)
                }
            } catch (e: Exception) {
                println("newBankAccount nie udało się utworzyć nowego konta bankowego: $e")
            }
        }
    }

    fun addBankAccount(bankAccountNumber: String, client: Client?) {
        viewModelScope.launch {
            try {
                if(client == null) throw NullPointerException("Client is null")
                val bankAccount = ClientBankAccount(
                    bankAccount = bankAccountNumber,
                    client = client,
                    createdAt = LocalDate.now()
                )
//                println("bankAccount przed zapisem: $bankAccount")
                val savedBankAccount: ClientBankAccount? = ApiClientsService.bankAccounts.saveClientBankAccount(
                    bankAccount
                )
                val refreshClient: Client? = ApiClientsService.clients.getClientData(client.id!!)

//                println("bankAccount zapisano rachunek: $savedBankAccount")
                _state.update { currentState ->
                    currentState.copy(bankAccount = null, client = refreshClient)
                }
            } catch (e: Exception) {
                println("addBankAccount nie udało się zapisać danych: $e")
            }
        }
    }

    fun deleteBankAccount(bankAccountNumber: String, client: Client?) {
        viewModelScope.launch {
            try {
                val accountNumber = bankAccountNumber.filterNot { it.isWhitespace() }
                val clientId: Long = client?.id ?: throw NullPointerException("Client is null")
                println("deleteBankAccount accountNumber: $accountNumber, client.id: $clientId")

                val isDeleted: Boolean = ApiClientsService.bankAccounts.deleteClientBankAccount(
                    clientId = clientId.toString(),
                    accountNumber = accountNumber,
                )

                val refreshClient: Client? = ApiClientsService.clients.getClientData(clientId)

                println("deleteBankAccount usunięto rachunek: $isDeleted")
                _state.update { currentState ->
                    currentState.copy(bankAccount = null, client = refreshClient)
                }
            } catch (e: Exception) {
                println("deleteBankAccount nie udało się usunąć konta: $e")
            }
        }
    }

    // change ForDate:
    fun updateForDate(date: LocalDate? = LocalDate.now()) {
        _state.update { currentState ->
            currentState.copy(forDate = date)
        }
    }

    // Drukuj faktury okresowe
    fun printAllInvoices(date: LocalDate? = LocalDate.now()){
        viewModelScope.launch {
            val isPrinting = ApiClientsService.invoices.printAllInvoice(date)
            println("isPrinting $isPrinting")
        }
    }

    fun onUnitPriceChanged(input: String) {
        val value = input.replace(',', '.').toDoubleOrNull()
        updatePosition(
            state.value.position?.copy(unitPrice = input)
        )
        value
    }

    fun ensureInvoiceCustomer() {
        val clientId = state.value.client?.id ?: return
        val invoice = state.value.invoice ?: return

        if (invoice.customer?.client?.id == clientId) return

        updateCustomerToInvoice(clientId)
    }

    // FINANCE
    // FINANCE LIST
    fun fetchListClientsFinance(page: Long = 0, size: Long = 100) {
        // TODO fetch list + update state
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    clientsWithPayments = ApiClientsService.paymentsListForFinanceTable.getPaymentsListForFinanceTable(page, size)
                )
            }

            println("clientsWithPayments: ${state.value.clientsWithPayments}")
        }
    }

    fun toFinanceList() {
        // fech client data
        getClientsList(0, 100)
        // update state
        _state.update { currentState ->
            currentState.copy(
//                clients = clients,
                currentScreen = CurrentScreen.FINANCES
            )
        }
    }

    fun rowsFinance(): List<TableRowFinance> {
        val clientsWithPayments: List<PaymentsListForFinanceTable> = state.value.clientsWithPayments
        println("rowsFinance: $clientsWithPayments")

        return clientsWithPayments.map { it ->
            val paymentsInMonth: List<PaymentForFinanceTable> = it.payments

            val grouped = paymentsInMonth
                .filter { !it.date.isNullOrBlank() }
                .groupBy { getMonthFinanceFromString(it.date!!) }

            TableRowFinance(
                name = it.client?.name ?: "brak nazwy!",
                values = grouped
            )
        }

    }

}
