package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.parkingandroidview.ui.DatePickerDocked
import com.example.parkingandroidview.ui.DepositOptionSelector
import com.kontenery.library.model.Contract
import com.kontenery.model.Product
import com.kontenery.model.Client
import com.kontenery.model.ClientOnList
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.util.to2Decimals
import com.kontenery.util.toDoublePl
import kotlinx.datetime.LocalDate

@Composable
fun ContractForm(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val client: Client? = state.client
    val contract: Contract? = state.contract
    var expandedClients by remember { mutableStateOf(false) }
    var expandedProducts by remember { mutableStateOf(false) }
    val clients: List<ClientOnList> = state.clients
    val products: List<Product> = state.products

    val clientEditable: Boolean = state.clientEnabled ?: true
    val productEditable: Boolean = state.productEnabled ?: true

    if(contract == null) LoadingBox()
    else Column(
        modifier = modifier.fillMaxWidth()
            .padding(4.dp)
            .verticalScroll(rememberScrollState())
    ) {

        OutlinedCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text("Klient: ",
                modifier = Modifier.padding(4.dp))
            ClientsDropdown(
                chosenClient = contract.client,
                selectClient = { selectedClient ->
                    viewModel.fetchClientForContract(selectedClient)
                    viewModel.updateContract(
                        contract.copy(
                            client = client,
                            needInvoice = client?.needInvoice() ?: false
                        )
                    )
                },
                clients = clients,
                expanded = expandedClients,
                toggleExpanded = { expandedClients = !expandedClients },
                enabled = clientEditable,
            )
        }
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (productEditable)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Produkt: ",
                modifier = Modifier.padding(4.dp))
            ProductDropdown(
                chosenProduct = contract.product,
                selectProduct = {selectedProduct ->
                    viewModel.updateContract(contract.copy(product = selectedProduct))},
                products = products,
                expanded = expandedProducts,
                toggleExpanded = { expandedProducts = !expandedProducts },
                enabled = productEditable
            )
        }
        ChooseDate(
            title = "Data rozpoczęcia",
            date = contract.startDate,
            updateDate = { date ->
                viewModel.updateContract(contract.copy(startDate = LocalDate.parse(date)))
            }
        )
        ChooseDate(
            title = "Data zakończenia",
            date = contract.endDate,
            updateDate = { date ->
                viewModel.updateContract(contract.copy(endDate = LocalDate.parse(date)))
            }
        )
        OutlinedTextField(
            value = if(contract.netPrice == null) 0.00.toString()
                else contract.netPrice!!.to2Decimals()
            ,
            onValueChange = {
                val newValue = it.toDoublePl()
                if(newValue != null) viewModel.updateContract(contract.copy(netPrice = it.toDoublePl()))
            },
            label = { Text("Cena netto za produkt:") },
            modifier = Modifier.fillMaxWidth()
        )
        DepositOptionSelector(
            viewModel,
            modifier
        )
//        println("need invoice ${contract.client?.needInvoice()}")
        BillType(
            needInvoice = contract.client?.needInvoice(),
            toggleInvoice = {
//                needInvoice: Boolean -> viewModel.updateContract(contract.copy(needInvoice = needInvoice))
            },
            enabled = false
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if(contract.id != null) {
                Button(onClick = {
                    viewModel.deleteContract(contract.id)
                    viewModel.toClientList()
                }) {
                    /*
                    Data zakońćzenia na koniec bierzącego miesiąca -> zapis w bazie danych
                     */
                    Text("Wypowiedz")
                }

                Spacer(Modifier.padding(8.dp))

                Button(onClick = {
                    println("PutContractToDB $contract")
                    viewModel.putContractToDB(contract)
                    viewModel.toClientList()
                }) {
                    Text("Uaktualnij")
                }
            } else {
                Button(onClick = {
                    println("saveContractToDB in contractForm: $contract")
                    viewModel.saveContractToDB(contract)
//                    viewModel.toClientList()
                }) {
                    Text("Podpisz")
                }
            }
        }
    }
}

@Composable
fun ChooseDate(
    title: String,
    modifier: Modifier = Modifier,
    date: LocalDate? = null,
    updateDate: (date: String) -> Unit,
) {
    Row(modifier = modifier) {
        DatePickerDocked(
            currentDate = date,
            updateDate = { date ->
                updateDate(date)
            },
            title = title,
        )
    }
}

@Composable
fun ProductDropdown(
    chosenProduct: Product? = null,
    selectProduct: (product: Product) -> Unit = {},
    products: List<Product>,
    expanded: Boolean = false,
    enabled: Boolean = true,
    toggleExpanded: () -> Unit = {}
) {
    // TODO dodaj guzik - WOLNE
    var search by remember { mutableStateOf("") }
    val filtered = remember(products, search) {
        products
            .filter { Product.createProductName(it).contains(search, ignoreCase = true) }
            .sortedBy { Product.createProductName(it) }
    }

    Box(modifier = Modifier) {
        TextButton(
            onClick = {toggleExpanded()},
            enabled = enabled,
        ) {
            OutlinedCard(modifier = Modifier) {
                if(chosenProduct == null)
                    Text("Znajdź Produkt: ")
                else
                    ProductRow(chosenProduct)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { toggleExpanded() }
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it; },
                label = { Text("Wybierz produkt") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            filtered.forEach { product ->
                DropdownMenuItem(
                    text = {
                        ProductRow(product)
                    },
                    onClick = {
                        selectProduct(product)
                        toggleExpanded()
                    }
                )
            }
            Spacer(Modifier.padding(16.dp))
        }
    }
}

@Composable
fun ProductRow(
    product: Product
) {
    OutlinedCard(
        modifier = Modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProductIcon(product)
                Spacer(modifier = Modifier.padding(8.dp))
                Text(product.name.toString())
                Spacer(modifier = Modifier.padding(8.dp))
                if(product is Product.Yard) {
                    Text(product.location.toString())
                    Spacer(modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun ClientsDropdown(
    chosenClient: Client? = null,
    selectClient: (clientId: Long) -> Unit = {},
    clients: List<ClientOnList>,
    expanded: Boolean = false,
    modifier: Modifier = Modifier,
    toggleExpanded: () -> Unit = {},
    enabled: Boolean = true,
) {
    // TODO dodaj guzik - Aktywni
    var search by remember { mutableStateOf("") }
    val filtered = remember(clients, search) {
        clients
            .filter { it.name.contains(search, ignoreCase = true) }
            .sortedBy { it.name }
    }

    Box(modifier = modifier) {
        TextButton(
            onClick = {toggleExpanded()}
            , enabled = enabled
        ) {
            OutlinedCard(modifier = modifier) {
                if(chosenClient == null)
                    Text("Wybierz klienta: ")
                else
                    Text(findById(chosenClient.id!!, clients)?.name.toString())
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { toggleExpanded() }
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Znajdź klienta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            filtered.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name) },
                    onClick = {
                        selectClient(client.id)
                        toggleExpanded()
                    }
                )
            }
        }
    }
}

private fun findById(id: Long, clients: List<ClientOnList>): ClientOnList? {
    return clients.find { it.id == id }
}

@Composable
fun BillType(
    modifier: Modifier = Modifier,
    needInvoice: Boolean? = null,
    toggleInvoice: (needInvoice: Boolean) -> Unit = {},
    enabled: Boolean = true,
) {
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround) {
        Column(modifier = modifier) {
            Row(modifier = modifier) {
                RadioButton(
                    selected = (needInvoice == true),
                    onClick = { toggleInvoice(needInvoice == true) },
                    enabled = enabled,
                )
                Text(
                    text = "Faktua",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        Column(modifier = modifier) {
            Row(modifier = modifier) {
                RadioButton(
                    selected = (needInvoice == false),
                    onClick = {toggleInvoice(false)}
                )
                Text(
                    text = "Rachunek",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun AddContract(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier,
    client: Client? = null,
    product: Product? = null,
) {
    val contract:Contract? = viewModel.state.collectAsState().value.contract
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OutlinedCard(
            modifier = Modifier
        ) {
            TextButton(
                onClick = {
                    if(client != null) viewModel.updateClient { client }
                    if(product != null) viewModel.updateProduct(product)
                    viewModel.updateContract(Contract(client = client, product = product))
                    viewModel.getProductsList()
                    viewModel.toAddContract()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Utwórz Umowę")
            }
    }
    }

}

//@Preview(showBackground = true, widthDp = 500)
//@Composable
//fun ContractFormPreview() {
//    ContractForm(
//        ParkingAppViewModel(),
//        Modifier.fillMaxSize()
//    )
//}
//
//@Preview(showBackground = true, widthDp = 500)
//@Composable
//fun AddContractPreview() {
//    AddContract(
//        ParkingAppViewModel(),
//        Modifier.fillMaxSize()
//    )
//}