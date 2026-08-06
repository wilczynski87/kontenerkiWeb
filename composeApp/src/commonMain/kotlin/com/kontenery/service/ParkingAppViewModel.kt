package com.kontenery.service

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.controller.ApiClientsService
import com.kontenery.data.AuthState
import com.kontenery.logDebug
import com.kontenery.logError
import com.kontenery.model.Client
import com.kontenery.model.ClientBankAccount
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientEvent
import com.kontenery.model.ClientPersonalData
import com.kontenery.model.Contract
import com.kontenery.model.Deposit
import com.kontenery.model.ModalData
import com.kontenery.model.Payment
import com.kontenery.model.PaymentDto
import com.kontenery.model.PaymentForFinanceTable
import com.kontenery.model.PaymentsListForFinanceTable
import com.kontenery.model.PrevYearBalance
import com.kontenery.model.Product
import com.kontenery.model.Product.Container
import com.kontenery.model.Product.Yard
import com.kontenery.model.Reading
import com.kontenery.model.Submeter
import com.kontenery.model.TableRowFinance
import com.kontenery.model.auth.LoginCredentials
import com.kontenery.model.auth.LoginResponse
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.enums.endOfCurrentYear
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentYear
import com.kontenery.model.errors.InvoiceErrorMessage
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.invoice.InvoiceFeature
import com.kontenery.model.invoice.Position
import com.kontenery.model.invoice.Subject
import com.kontenery.model.invoice.Subject.Seller
import com.kontenery.ui.login.ServerConnectivity
import com.kontenery.util.endOfYear
import com.kontenery.util.getMonthFinanceFromString
import com.kontenery.util.startOfYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ParkingAppViewModel(
    private val coroutineScope: CoroutineScope,
    autoInitialize: Boolean = true,
    initialState: ParkingAppState = ParkingAppState(),
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ParkingAppState> = _state.asStateFlow()
    private val modalController = ParkingModalController(_state)
    private val navigationController = ParkingNavigationController(_state)
    private val selectionController = ParkingSelectionController(_state)
    private val invoiceDraftController = InvoiceDraftController(_state)
    private val clientEventHandler = ClientEventHandler(
        updateClient = ::updateClient,
        saveClient = ::save,
        updateExistingClient = ::update,
    )
    private val authService = AuthService()

    private var currentPage = 0
    private var isLoading = false
    private var endReached = false
    private val pageSize = 100

    init {
        if (autoInitialize) {
            initializeUiState()
        }
    }

    private fun initializeUiState() {
        _state.value = ParkingAppState(clientNavRow = 1L)
        coroutineScope.launch {
            bootstrapAuth()
        }
    }

    private suspend fun bootstrapAuth() {
        val connectivity = runServerConnectivityCheck()
        if (connectivity is ServerConnectivity.Online) {
            restoreSession()
        } else {
            logDebug("login", "skip restoreSession — server offline")
            _state.update {
                it.copy(authState = AuthState(loading = false, error = null))
            }
        }
    }

    private suspend fun runServerConnectivityCheck(): ServerConnectivity {
        _state.update {
            it.copy(loginUi = it.loginUi.copy(serverConnectivity = ServerConnectivity.Checking(null)))
        }
        val connectivity = authService.checkServerConnectivity { url ->
            logDebug("serverHealthCheck", "probing $url")
            _state.update {
                it.copy(loginUi = it.loginUi.copy(serverConnectivity = ServerConnectivity.Checking(url)))
            }
        }
        logDebug("serverHealthCheck", "result=$connectivity baseUrl=$baseUrl")
        _state.update { it.copy(loginUi = it.loginUi.copy(serverConnectivity = connectivity)) }
        return connectivity
    }
    /*
        MODAL
     */
    fun closeConfirmationModal() {
        modalController.closeConfirmationModal()
    }

    fun showConfirmModal(
        dialogTitle: String,
        dialogText: String,
        onConfirmation: () -> Unit,
    ) {
        modalController.showConfirmModal(dialogTitle, dialogText, onConfirmation)
    }

    fun showErrorModal(
        dialogTitle: String,
        dialogText: String,
        onDismissRequest: () -> Unit = {closeConfirmationModal()},
        onConfirmation: () -> Unit,
    ) {
        modalController.showErrorModal(dialogTitle, dialogText, onDismissRequest, onConfirmation)
    }

    fun createConfirmationModal(
        modal: ModalData = ModalData(onDismissRequest = {closeConfirmationModal()})
    ) {
        modalController.createConfirmationModal(modal)
    }

    fun closeResponseModal() {
        modalController.closeResponseModal()
    }

    fun onLoginUsernameChange(value: String) {
        _state.update {
            it.copy(
                loginUi = it.loginUi.copy(
                    username = value,
                    usernameError = null,
                ),
            )
        }
    }

    fun onLoginPasswordChange(value: String) {
        _state.update {
            it.copy(
                loginUi = it.loginUi.copy(
                    password = value,
                    passwordError = null,
                ),
            )
        }
    }

    fun onLoginPasswordVisibilityToggle() {
        _state.update {
            it.copy(loginUi = it.loginUi.copy(isPasswordVisible = !it.loginUi.isPasswordVisible))
        }
    }

    fun checkServerConnectivity() {
        coroutineScope.launch {
            runServerConnectivityCheck()
        }
    }

    fun submitLogin() {
        val form = _state.value.loginUi
        val validation = authService.validateForm(form.username, form.password)
        if (!validation.isValid) {
            _state.update {
                it.copy(
                    loginUi = it.loginUi.copy(
                        usernameError = validation.usernameError,
                        passwordError = validation.passwordError,
                    ),
                )
            }
            return
        }

        val connectivity = form.serverConnectivity
        if (connectivity !is ServerConnectivity.Online) {
            _state.update {
                it.copy(authState = AuthState(error = "Serwer niedostępny — sprawdź połączenie"))
            }
            return
        }

        coroutineScope.launch {
            _state.update { it.copy(authState = AuthState(loading = true, error = null)) }
            authService.login(LoginCredentials(form.username, form.password))
                .onSuccess { user ->
                    getClientsList(0, 100)
                    _state.update {
                        it.copy(
                            authState = AuthState(
                                isAuthenticated = true,
                                user = LoginResponse(user.id, user.role),
                                loading = false,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    logError("login", "failed: $error")
                    if (isSecureStorageFailure(error)) {
                        authService.clearSession()
                    }
                    _state.update {
                        it.copy(
                            authState = AuthState(
                                isAuthenticated = false,
                                loading = false,
                                error = authService.mapLoginFailure(error),
                            ),
                        )
                    }
                }
        }
    }

    /*
        SUBHEADING
     */
    fun updateSubheading(subheading:String? = null) {
        _state.update { state ->
            state.copy(subheading = subheading)
        }
    }

    /*
        BACK BUTTON
     */

    fun setGoBack(targetScreen: CurrentScreen, triggerScreen: CurrentScreen) {
        navigationController.setGoBack(targetScreen, triggerScreen)
    }
    fun goBack() {
        navigationController.goBack()
    }
    fun checkGoBack() {
        navigationController.checkGoBack()
    }

    /*
        ClientOnList
     */

    fun toggleClientNavRow(clientId: Long) {
        selectionController.toggleClientNavRow(clientId)
    }

//    fun getClientsList(page: Int, size: Int) {
//        val page: Int? = null
//        val size: Int = 10
//        viewModelScope.launch {
//            try {
//                val clientsCount: Long = ApiClientsService.clients.clientListSize()
//                val pagesCount: Int = (clientsCount / size).toInt()
//                _state.update { currentState ->
//                    currentState.copy(clients = mutableListOf())
//                }
//                for(i in 0..pagesCount) {
//                    val clients: List<ClientOnList> = ApiClientsService.clients.getClientList(i, size)
////                    println("getClientsList", "dane: $clients")
//                    _state.update { currentState ->
//                        currentState.copy(
//                            clients = (currentState.clients + clients).distinctBy { it.id }
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                println("getClientsList nie udało się pobrać danych $e")
//                _state.update { currentState ->
//                    currentState.copy(clientListError = true)
//                }
//            }
//        }
//    }

    fun getClientsList(page: Int, size: Int) {
        if (isLoading || endReached) return

        coroutineScope.launch {
            isLoading = true

            try {
                while(isLoading && !endReached) {
                    logDebug("getClientsList", "getClientsList: $currentPage, isLoading: $isLoading, endReached: $endReached")

                    val newClients = ApiClientsService.clients.getClientList(currentPage, pageSize)

                    if (newClients.isEmpty()) {
                        endReached = true
                    } else {
                        _state.update { currentState ->
                            val merged = (currentState.clients + newClients)
                                .associateBy { it.id }
                                .values
                                .toList()

                            currentState.copy( clients = merged )
                        }
                        currentPage++
                    }
                }


            } catch (e: Exception) {
                println("getClientsList error: $e")

                _state.update { currentState ->
                    currentState.copy(clientListError = true)
                }

            } finally {
                isLoading = false
            }
        }
    }

    fun toggleClientsListModal() {
        selectionController.toggleClientsListModal()
    }

    fun toClientData(idClient: Long? = null) {
        // fech client data by Id
        coroutineScope.launch {
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

    fun getClientNameById(clientId: Long): String? {
        return selectionController.getClientNameById(clientId)
    }

//    fun updateClient(client: Client) {
//        _state.update { currentState ->
//            currentState.copy(client = client)
//        }
//    }
    fun updateClient(update: (Client) -> Client) {
        _state.value.client?.let { client ->
            _state.update { currentState ->
                currentState.copy(client = update(client))
            }
        }
    }

//    fun updateClient(clientId: Long?) {
//        viewModelScope.launch {
//            try {
//                val client: Client = ApiClientsService.clients.getClientData(clientId!!)
//
//                // fetch client data by Id
//                _state.update { currentState ->
//                    currentState.copy(client = client)
//                }
//            } catch (e: Exception) {
//                println("updateClientError $e")
//            }
//        }
//    }
    fun fetchClient(clientId: Long?) {
        if (clientId == null) return

        coroutineScope.launch {
            try {
                val client: Client = ApiClientsService.clients.getClientData(clientId)
                _state.update { currentState ->
                    currentState.copy(client = client)
                }
            } catch (e: Exception) {
                println("fetchClientError: $e")
            }
        }
    }

    fun fetchPaymentsForClient(
        clientId: Long,
        from: LocalDate? = LocalDate.startOfCurrentYear(),
        to: LocalDate? = LocalDate.endOfCurrentYear()
    ) {
        coroutineScope.launch {
            try {
//                logDebug("fetchPaymentsForClient:", "clientId: $clientId, from: $from, to: $to")
                val payments = ApiClientsService.payments.getPaymentsForClient(
                    clientId,
                    from.toString(),
                    to.toString()
                )
//                logDebug("fetchForClientPayments", "dane: $payments")
                _state.update { currentState ->
                    currentState.copy(payments = payments)
                }
            } catch (e: Exception) {
                logError("fetchPaymentsForClient","Can not fetch payments: $e")
            }
        }
    }

    fun fetchInvoicesForClient(clientId: Long, from: LocalDate? = LocalDate.startOfCurrentYear(), to: LocalDate? = LocalDate.endOfCurrentYear()) {
        coroutineScope.launch {
            try {
//                println("fetchForClientInvoices from: $from, to: $to, clientId: $clientId")
                val invoices: List<Invoice> =
                    ApiClientsService.invoices.fetchInvoicesForClient(
                        clientId,
                        from.toString(),
                        to.toString(),
                    )
//                println("fetchForClientInvoices: dane: $invoices")
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

    fun fetchClientFinance(
        clientId: Long,
        from: LocalDate? = LocalDate.startOfCurrentYear(),
        to: LocalDate? = LocalDate.endOfCurrentYear()
    ) {
        coroutineScope.launch {
            try {
                val clientFinance: PrevYearBalance = ApiClientsService.clients.clientFinance(clientId, from, to)
                    ?: throw NullPointerException("Can not fetch client finance")
                _state.update { currentState ->
                    currentState.copy(prevYearsBalance = clientFinance)
                }

            } catch (e: Exception) {
                println("fetchClientFinance nie udało się pobrać danych $e")
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
        coroutineScope.cancel()
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
        coroutineScope.launch {
            try {
                val stateClient: Client? = state.value.client

                if (stateClient != null && stateClient.id != null) {
                    val updatedClient: Client = ApiClientsService.clients.updateClient(
                        stateClient.id,
                        stateClient
                    )

                    println("putClient zaktualizowano klienta: $updatedClient")

                    _state.update { currentState ->
                        currentState.copy(client = updatedClient)
                    }
                } else println("putClient nie udało się zaktualizować klienta: $stateClient")

            } catch (e: Exception) {
                println("putClient nie udało się zaktualizować danych klienta, błąd: $e")
            }
        }
    }

    fun saveClient(client: Client) {
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        selectionController.newProduct(product)
    }

    fun updateProduct(product: Product) {
        selectionController.updateProduct(product)
    }

    fun clearProduct() {
        selectionController.clearProduct()
    }

    fun toggleProductNavRow(productId: Long?) {
        selectionController.toggleProductNavRow(productId)
    }


    fun getClientOverdue(clientId: Long): Double? {
        val hasClient = state.value.clients.any { it.id == clientId }
        if (!hasClient) {
            getClientsList(0, 1000)
        }
        return selectionController.getClientOverdue(clientId)
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
            try {
                val contract: Contract =
                    ApiClientsService.contracts.getContractByProductId(productId)
                        ?: throw NullPointerException("Can not find contract with id: $productId")
//                println("fetchContractByProductId $contract")

                updateContract(contract)

                toContractMenu(contract.id, productEnabled = false, clientEnabled = true)

            } catch (e: Throwable) {
                println("fetchContractsForClient nie udało się pobrać danych $e")
            }
        }
    }

    // TODO POPRAWIć - daje 400
    fun saveContractToDB(contract: Contract) {
        // save contract to DB
        coroutineScope.launch {
            try {
//                println("saveContractToDB Przed zapisem: $contract")
                println("saveContractToDB rzed zapisem: ${contract.toContractDTO()}")
                val result = ApiClientsService.contracts.postContract(contract.toContractDTO())
                if(result.isSuccess) {
                    _state.update { currentState ->
                        currentState.copy(contract = result.getOrNull())
                    }
                    println("saveContractToDB zapisano umowę: $result")
                    toClientList()
                } else {
                    println("Zapisanie kontratu nie pykło... ${result.exceptionOrNull()}")
                    showErrorModal(
                        "Zapisanie kontratu nie pykło, ponieważ:",
                        "${result.exceptionOrNull()}",
                    ) {
                        closeConfirmationModal()
                        toClientList()
                    }
                }
            } catch (e: Throwable) {
                println("saveContractToDB nie udało się pobrać danych $e")
            }
        }
    }

    fun putContractToDB(contract: Contract) {
        // update contract to DB
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        contract = contract!!.copy(deposit = newDeposit)
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
        coroutineScope.launch {
            try {
                val response = ApiClientsService.invoices.postPeriodicInvoice(
                    clientId = clientId,
                    period = state.value.forDate?.toString(),
                )
                handlePeriodicInvoiceResponse(
                    response = response,
                    successTitle = "Faktura okresowa wysłana",
                    successText = "Faktura okresowa została wygenerowana i wysłana do klienta.",
                )
            } catch (e: Exception) {
                println("sendPeriodicInvoice nie udało się wysłać faktury $e")
                showErrorModal(
                    dialogTitle = "Faktura okresowa NIE została wysłana",
                    dialogText = e.message ?: "Nie udało się połączyć z serwerem.",
                    onConfirmation = {},
                )
            }
        }
    }

    fun sendInvoiceToKsef(invoice: Invoice) {
        coroutineScope.launch {
            ApiClientsService.ksef.sendInvoice(invoice).onSuccess { response ->
                showConfirmModal(
                    dialogTitle = "Faktura wysłana do KSeF",
                    dialogText = buildString {
                        append("Numer faktury: ${response.invoiceNumber ?: invoice.invoiceNumber}")
                        response.ksefNumber?.let { append("\nNumer KSeF: $it") }
                        append("\nReferencja sesji: ${response.sessionReferenceNumber}")
                    },
                    onConfirmation = {
                        state.value.clientNavRow?.let { fetchInvoicesForClient(it) }
                    },
                )
            }.onFailure { e ->
                showErrorModal(
                    dialogTitle = "Faktura NIE wysłana do KSeF",
                    dialogText = e.message ?: "Nieznany błąd",
                    onConfirmation = {},
                )
            }
        }
    }

    // TODO obsługa odpowiedzi do napisania
    fun postPeriodicInvoiceAgain(invoiceNumber: String) {
        coroutineScope.launch {
            println("postPeriodicInvoiceAgain resp for: $invoiceNumber:\n")
            ApiClientsService.invoices.postPeriodicInvoiceAgain(invoiceNumber).onSuccess { invoiceSend ->
                showConfirmModal(
                    dialogTitle = "Faktura wysłana ponownie",
                    dialogText = "numer: ${invoiceSend.invoiceNumber} \ndla klienta: ${invoiceSend.forClient} \n wysłana: ${invoiceSend.sendLastTime}",
                    onConfirmation = {}
                )
            }.onFailure { e ->
                showErrorModal(
                    dialogTitle = "Faktura NIE wysłana ponownie...",
                    dialogText = "${e.message}",
                    onConfirmation = {}
                )
            }
        }
    }

    fun sendInvoiceToKsef(invoiceNumber: String) {
        coroutineScope.launch {
            ApiClientsService.invoices.sendInvoiceToKsef(invoiceNumber).onSuccess { result ->
                showConfirmModal(
                    dialogTitle = "Faktura wysłana do KSeF",
                    dialogText = buildString {
                        append("Numer faktury: ${result.invoiceNumber ?: invoiceNumber}")
                        result.ksefNumber?.let { append("\nNumer KSeF: $it") }
                        result.status?.let { append("\nStatus: $it") }
                        result.sentAt?.let { append("\nData wysłania: $it") }
                        result.message?.let { append("\n$it") }
                    },
                    onConfirmation = {
                        state.value.clientNavRow?.let { clientId ->
                            fetchInvoicesForClient(clientId)
                        }
                    },
                )
            }.onFailure { e ->
                showErrorModal(
                    dialogTitle = "Faktura NIE wysłana do KSeF",
                    dialogText = e.message ?: "Nieznany błąd",
                    onConfirmation = {},
                )
            }
        }
    }

    fun sendPeriodicInvoiceToAllClients(period: LocalDate = LocalDate.now()) {
        coroutineScope.launch {
            try {
                val response = ApiClientsService.invoices.postPeriodicInvoiceToAllClients(period.toString())
                handlePeriodicInvoiceResponse(
                    response = response,
                    successTitle = "Faktury okresowe wysłane",
                    successText = "Faktury okresowe za okres $period zostały wygenerowane i wysłane.",
                )
            } catch (e: Exception) {
                println("sendPeriodicInvoiceToAllClients nie udało się wysłać faktur $e")
                showErrorModal(
                    dialogTitle = "Faktury okresowe NIE zostały wysłane",
                    dialogText = e.message ?: "Nie udało się połączyć z serwerem.",
                    onConfirmation = {},
                )
            }
        }
    }

    private fun handlePeriodicInvoiceResponse(
        response: List<InvoiceErrorMessage>,
        successTitle: String,
        successText: String,
    ) {
        if (response.isEmpty()) {
            showConfirmModal(
                dialogTitle = successTitle,
                dialogText = successText,
                onConfirmation = {},
            )
        } else {
            _state.update { currentState ->
                currentState.copy(responseErrors = response)
            }
        }
    }

    // TODO do wykasowania!!!
    fun updateInvoicesAndPayments(invoices: List<Invoice>, payments: List<Payment>) {
        selectionController.updateInvoicesAndPayments(invoices, payments)
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

    fun createNewInvoice(invoiceType: InvoiceType? = null, clientId: Long? = null) {
        invoiceDraftController.createNewInvoice(invoiceType)
    }

    fun updateInvoice(invoice: Invoice) {
        invoiceDraftController.updateInvoice(invoice)
    }

    fun sellerForInvoiceUpdate() {
        invoiceDraftController.sellerForInvoiceUpdate()
    }

    fun addProductToInvoice(position: Position? = null) {
        invoiceDraftController.addProductToInvoice(position)
    }

    fun removeProductFromInvoice(index: Int) {
        invoiceDraftController.removeProductFromInvoice(index)
    }

    fun updatePosition(position: Position?) {
        invoiceDraftController.updatePosition(position)
    }

    fun calculatePosition(position: Position) {
        invoiceDraftController.calculatePosition(position)
    }

    fun postCustomInvoice(clientId: Long, invoice: Invoice) {
        // save invoice to DB
        coroutineScope.launch {
            try {
//                val invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: postCustomInvoice")
                println("saveCustomInvoiceToDB $invoice")
//                var invoiceNew = invoice
//                var inv = gson.toJson(invoiceNew)
//                println("saveCustomInvoiceToDB json: $inv")
                val savedInvoice = ApiClientsService.invoices.postCustomInvoice(clientId, invoice)
                if(savedInvoice != null) {
                    showConfirmModal(
                        "Status dodatkowej faktury:",
                        "Faktura o numerze: ${savedInvoice.invoiceNumber}, zapisana w bazie!",
                    ) {
                        closeConfirmationModal()

                        fetchClient(clientId)
                        fetchPaymentsForClient(clientId)
                        fetchInvoicesForClient(clientId)
                        toPaymentsMenu()
                    }
                }
                // TODO dać info o zapisanej fakturze
            } catch (e: Exception) {
                println("saveCustomInvoiceToDB nie udało się wysłać faktury $e")
                showConfirmModal(
                    "Status dodatkowej faktury:",
                    "Błąd przy wysłaniu faktury - dokument nie zapisany!",
                ) {
                    closeConfirmationModal()
                }
            }
        }
    }

    fun updateCustomerToInvoice(clientId: Long) {
        val invoice: Invoice = state.value.invoice ?: throw NullPointerException("Invoice is null, for: updateCustomerToInvoice")
        coroutineScope.launch {
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

    // UTILITY
    fun toUtility() {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.UTILITY
            )
        }
    }

    fun toReadings() {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.READINGS
            )
        }
    }

    fun toSubmeter() {
        _state.update { currentState ->
            currentState.copy(
                currentScreen = CurrentScreen.ADD_SUBMETER
            )
        }
    }

    fun choseInvoice(invoiceFeature: InvoiceFeature) {
        _state.update { currentState ->
            currentState.copy(
                invoiceFeature = invoiceFeature
            )
        }
    }

    fun fetchSubmetersForClient(clientId: Long) {
        coroutineScope.launch {
            ApiClientsService.utilities.fetchSubmetersForClient(clientId)
                .onSuccess { result ->
                    _state.update { state ->
                        state.copy(
                            submeters = result
                        )
                    }
                }
                .onFailure { e ->
                    println("Error when fetchSubmetersForClient: ${e.message}")
                    _state.update { state ->
                        state.copy(
                            // TODO po utworzeniu endpointu na backendze - odblokować
//                            submeters = emptyList()
                        )
                    }
                }
        }
    }

    fun postSubmeterReading(submeterId: Long, newReading: Reading) {
        coroutineScope.launch {
            ApiClientsService.utilities.addReadingToSubmeter(submeterId, newReading)
                .onSuccess { submeter ->
                    if (submeter.clientId == null) throw NullPointerException("No client Id for submeter")
                    _state.update { state ->
                        state.copy(
                            submeter = submeter,
                        )
                    }
                }
                .onFailure { e ->
                    println("Could not add Reading to submeter: $e")
                    showErrorModal(
                        "Could not add reading",
                        "${e.message}",
                        onConfirmation = {closeConfirmationModal()},
                    )
                }
        }
    }

    fun postSubmeterReadings(newReadings: List<Reading>) {
        coroutineScope.launch {
            newReadings.forEach { newReading ->
                ApiClientsService.utilities.addReadingToSubmeter(
                    newReading.submeterId ?: throw NullPointerException("No submeter Id for reading: $newReading") ,
                    newReading
                ).onSuccess { submeter ->
                        if (submeter.clientId == null) throw NullPointerException("No client Id for submeter")
                        _state.update { state ->
                            state.copy(
                                submeter = null,
                            )
                        }
                    }
                    .onFailure { e ->
                        println("Could not add Reading to submeter: $e")
                        showErrorModal(
                            "Could not add reading",
                            "${e.message}",
                            onConfirmation = {closeConfirmationModal()},
                        )
                    }
            }
            fetchSubmeters()
            _state.update { state ->
                state.copy(readings = emptyList())
            }
        }
    }

    fun fetchSubmeters() {
        coroutineScope.launch {
            println("fetchSubmeters")
            val submeters = ApiClientsService.utilities.getAllSubmeters()
                _state.update { state ->
                    state.copy(submeters = submeters)
                }
        }
    }
    fun fetchSubmeter(submeterId: Long) {
        coroutineScope.launch {
            ApiClientsService.utilities.getSubmeter(submeterId)
                .onSuccess {
                    _state.update { state ->
                        state.copy(submeter = it)
                    }
                }.onFailure { e ->
                    println("fetchSubmeter error: ${e.message}")
                }
        }
    }

    fun postSubmeter(submeter: Submeter) {
        coroutineScope.launch {
            ApiClientsService.utilities.postSubmeter(submeter)
                .onSuccess { submeters ->

                    _state.update { state ->
                        state.copy(
                            submeters = submeters,
                            currentScreen = CurrentScreen.UTILITY
                        )
                    }
                }.onFailure { e ->
                    println("postSubmeter error: ${e.message}")
                }
        }
    }

    fun updateSubmeter(submeterId: Long, submeter: Submeter) {
        coroutineScope.launch {
            ApiClientsService.utilities.putSubmeter(submeterId, submeter)
                .onSuccess {
                    _state.update { state ->
                        state.copy(submeter = it)
                    }
                }.onFailure { e ->
                    println("updateSubmeter error: ${e.message}")
                }
        }
    }

    fun updateSubmeterClient(clientId: Long, submeter: Submeter) {
        coroutineScope.launch {
            try {
                val submeterId = submeter.id ?: throw NullPointerException("Brak id podlicznika")

                ApiClientsService.utilities.putSubmeter(submeterId, submeter)
                    .onSuccess { submeter ->
                        val client: Client? = ApiClientsService.clients.getClientData(clientId)
                        println("fetchClientSubmeter $client")
                        _state.update { state ->
                            state.copy(
                                client = client,
                                submeter = submeter
                            )
                        }
                    }.onFailure { e ->
                        println("updateSubmeterClient error: ${e.message}")
                    }
            } catch (e: Exception) {
                println("fetchClientById nie udało się odnaleźć danych, o id: $clientId,\n $e")
            }
        }
    }

    fun checkReading(clientId: Long, reading: Reading) {
        coroutineScope.launch {
            val submitContentMap = state.value.submitContentMap.toMutableMap()
            val submitContent: Map<String, SubmitContent> = mapOf("checkReading" to SubmitContent(true))
            _state.update { state ->
                state.copy(submitContentMap = submitContentMap.plus(submitContent))
            }

            ApiClientsService.utilities.checkReading(clientId, reading)
                .onSuccess { reading ->
                    val submitContentMap = state.value.submitContentMap.toMutableMap()
                    submitContentMap.remove("checkReading")
                    _state.update { state ->
                        state.copy(reading = reading, submitContentMap = submitContentMap)
                    }
                }
                .onFailure { e ->
                    println("error: checkReading")
                    val submitContent: Map<String, SubmitContent> = mapOf("checkReading" to SubmitContent(false, listOf(e.message ?: "")))
                    _state.update { state ->
                        state.copy(submitContentMap = submitContentMap.plus(submitContent))
                    }
                }
        }
    }

    fun deleteReading(submeterId: Long, readingId: Long) {
        coroutineScope.launch {
            try {
                ApiClientsService.utilities.deleteReadingToSubmeter(readingId)
                    .onSuccess {
                        ApiClientsService.utilities.getSubmeter(submeterId)
                            .onSuccess { submeter ->
                                _state.update { state ->
                                    state.copy(
                                        submeter = submeter
                                    )
                                }
                            }
                            .onFailure { e ->
                                println(e)
                                throw e
                            }
                    }.onFailure { e ->
                        println(e)
                        throw e
                    }

            } catch (e: Throwable) {
                println("deleteReading ERROR $readingId, $e")
            }
        }
    }

    // TODO
    fun createPositionFromReading(clientId: Long, reading: Reading) {
        val submitContentMap = state.value.submitContentMap.toMutableMap()
        val key = "createProductFromReading-${reading.utilityType?.name}-$clientId"
        val submitContentToAdd = SubmitContent(true)
        _state.update { state ->
            state.copy(submitContentMap = submitContentMap.plus(key to submitContentToAdd))
        }

        coroutineScope.launch {
            ApiClientsService.utilities.createPositionFromReading(clientId, reading)
                .onSuccess { position ->
                    println("position add to invoice: $position")
                    val readings = state.value.readings.toMutableList().plus(reading)
//                    val products: MutableList<Position> = state.value.invoice?.products?.toMutableList() ?: mutableListOf()
//                    val invoice = state.value.invoice?.copy(products = products.plus(position))
                    _state.update { state ->
                        state.copy(submitContentMap = submitContentMap, position = position, readings = readings)
                    }.also {
                        addProductToInvoice()
                    }
                }
                .onFailure { e ->
                    println("error: checkReading")
                    _state.update { state ->
                        state.copy(submitContentMap = submitContentMap.plus(key to SubmitContent(false, listOf(e.message ?: ""))))
                    }
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
        coroutineScope.launch {
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
        coroutineScope.launch {
            try {
                println("postPayment $payment")
                val paymentSaved = ApiClientsService.payments.postPayment(payment)
            } catch (e: Exception) {
                println("postPayment nie udało się zapisać płatności,\n $e")
            }
        }
    }
    fun postPaymentToApiWithResponse(payment: PaymentDto) {
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        coroutineScope.launch {
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
        selectionController.updateForDate(date)
    }

    // Drukuj faktury okresowe
    fun printAllInvoices(date: LocalDate? = LocalDate.now()){
        coroutineScope.launch {
            try {
                val isPrinting = ApiClientsService.invoices.printAllInvoice(date)
                println("isPrinting $isPrinting")
                if (isPrinting) {
                    showConfirmModal(
                        dialogTitle = "Faktury zostały wydrukowane",
                        dialogText = "Faktury za okres $date zostały przekazane do wydruku.",
                        onConfirmation = {},
                    )
                } else {
                    showErrorModal(
                        dialogTitle = "Drukowanie faktur nie powiodło się",
                        dialogText = "Serwer nie potwierdził wydruku faktur.",
                        onConfirmation = {},
                    )
                }
            } catch (e: Exception) {
                showErrorModal(
                    dialogTitle = "Drukowanie faktur nie powiodło się",
                    dialogText = e.message ?: "Nie udało się połączyć z serwerem.",
                    onConfirmation = {},
                )
            }
        }
    }

    fun onUnitPriceChanged(input: String) {
        invoiceDraftController.onUnitPriceChanged(input)
    }

    fun ensureInvoiceCustomer() {
        val clientId = state.value.client?.id ?: return
        val invoice = state.value.invoice ?: return

        if (invoice.customer?.client?.id == clientId) return

        updateCustomerToInvoice(clientId)
    }

    // FINANCE
    // FINANCE LIST
//    fun fetchListClientsFinance(page: Long = 0, size: Long = 100) {
//        // TODO fetch list + update state
//        viewModelScope.launch {
//            _state.update { currentState ->
//                currentState.copy(
//                    clientsWithPayments = ApiClientsService.paymentsListForFinanceTable.getPaymentsListForFinanceTable(page, size)
//                )
//            }
//        }
//    }

    fun fetchListClientsFinance(page: Long = 0, size: Long = 20, fromDate: LocalDate? = null, toDate: LocalDate? = null) {
        var isLoading = false
        var endReached = false
        var currentPage = page
//        logDebug("Finance", "page: $page, size: $size, fromDate: $fromDate, toDate: $toDate, isLoading: $isLoading, endReached: $endReached")

        if (isLoading || endReached) return

        coroutineScope.launch {
            isLoading = true

            try {
                while(isLoading && !endReached) {
                    logDebug("Finance", "fetchListClientsFinance: $currentPage, isLoading: $isLoading, endReached: $endReached")

                    val newClientsWithPayments = ApiClientsService.paymentsListForFinanceTable.getPaymentsListForFinanceTable(currentPage, size, fromDate, toDate)

                    if (newClientsWithPayments.isEmpty()) {
                        endReached = true
                    } else {
                        _state.update { currentState ->
                            val merged = (currentState.clientsWithPayments + newClientsWithPayments)
                                .associateBy { it.client?.clientId }
                                .values
                                .toList()

                            currentState.copy(
                                clientsWithPayments = merged,
                                financeTable = rowsFinance(merged)
                            )
                        }
                        currentPage++
                    }
                }
            } catch (e: Exception) {
                println("getClientsList error: $e")

                _state.update { currentState ->
                    currentState.copy(clientListError = true)
                }

            } finally {
                _state.update { currentState ->
                    currentState.copy(
                        financeTable = rowsFinance()
                    )
                }
                endReached = false
                isLoading = false
            }
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

    fun rowsFinance(clientsWithPayments: List<PaymentsListForFinanceTable>? = null): List<TableRowFinance> {
        val clientsWithPayments: List<PaymentsListForFinanceTable> = clientsWithPayments ?: state.value.clientsWithPayments
        val sortedList = clientsWithPayments.sortedWith (
            compareByDescending<PaymentsListForFinanceTable> { it.client?.isActive }
                .thenBy { it.client?.clientId }
        )

        return sortedList.map { it ->
            val paymentsInMonth: List<PaymentForFinanceTable> = it.payments

            val grouped = paymentsInMonth
                .filter { !it.date.isNullOrBlank() }
                .groupBy { getMonthFinanceFromString(it.date!!) }

            TableRowFinance(
                clientId = it.client?.clientId,
                name = it.client?.name ?: "brak nazwy!",
                values = grouped,
                isActive = it.client?.isActive ?: true,
                prevYearsBalance = it.lastYearsBalance,
                clientOverdue = it.clientOverdue,
            )
        }

    }

    fun onFinanceYearChange(year: Int) {
        coroutineScope.launch {
            fetchListClientsFinance(0, 100, startOfYear(year), endOfYear(year))
            _state.update { currentState ->
                currentState.copy(financeYear = year)
            }
        }
    }
    fun changeFinanceYear(year: Int) {
        selectionController.changeFinanceYear(year)
    }
    fun changeFinanceYearPaymentsMenu(year: Int) {
        val from = LocalDate.parse("${year}-01-01")
        val to = if(year == LocalDate.now().year) LocalDate.now()
            else LocalDate.parse("${year}-12-31")
        val clientId = state.value.client?.id ?: return

        coroutineScope.launch {
            fetchInvoicesForClient(clientId, from, to)
            fetchPaymentsForClient(clientId, from, to)
            fetchClientFinance(clientId, from.minus(1, DateTimeUnit.YEAR),
                LocalDate(year = (to.year - 1), month = 12, day = 31)
            )
        }
        _state.update { currentState ->
            currentState.copy(financeYear = year)
        }
    }

    fun restoreSession() {
        coroutineScope.launch {
            _state.update { it.copy(authState = AuthState(loading = true, error = null)) }
            authService.restoreSession()
                .onSuccess { user ->
                    logDebug("login", "restoreSession success")
                    getClientsList(0, pageSize)
                    _state.update {
                        it.copy(
                            authState = AuthState(
                                isAuthenticated = true,
                                user = user,
                                loading = false,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    logError("login", "restoreSession failed: $error")
                    authService.clearSession()
                    _state.update {
                        it.copy(
                            authState = AuthState(
                                isAuthenticated = false,
                                loading = false,
                                error = null,
                                user = null,
                            ),
                        )
                    }
                }
        }
    }

    fun onClientEvent(event: ClientEvent) {
        clientEventHandler.handle(event)
    }

    private fun save() {
        val client = state.value.client ?: return
        saveClient(client)
        toClientList()
    }

    private fun update() {
        putClient()
        toClientList()
    }
}

private fun isSecureStorageFailure(error: Throwable): Boolean {
    var current: Throwable? = error
    while (current != null) {
        val name = current::class.simpleName.orEmpty()
        if (name.contains("InvalidKey", ignoreCase = true)
            || name.contains("GeneralSecurity", ignoreCase = true)
            || name.contains("KeyStore", ignoreCase = true)
        ) {
            return true
        }
        if (current.message?.contains("invalid key", ignoreCase = true) == true) {
            return true
        }
        current = current.cause
    }
    return false
}
