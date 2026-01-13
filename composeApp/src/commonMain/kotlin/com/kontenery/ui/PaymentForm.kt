package com.example.parkingandroidview.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.model.PaymentDto
import com.kontenery.model.PaymentMethod
import com.kontenery.ui.ChooseDate
import com.kontenery.ui.ClientsDropdown
import kotlinx.datetime.LocalDate
import com.kontenery.model.Client
import com.kontenery.model.ClientOnList
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.service.isDigitsOnly
import com.kontenery.service.toDoublePl

@Composable
fun PaymentForm(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val payment: PaymentDto? = viewModel.state.collectAsState().value.payment

    val client: Client? = viewModel.state.collectAsState().value.client
    if(client != null && payment?.fromClientId == null) viewModel.newPaymentState(client.id)

    val clients: List<ClientOnList> = viewModel.state.collectAsState().value.clients
    if(clients.isEmpty()) viewModel.getClientsList(0, 1000)

    val changeClient: Boolean = viewModel.state.collectAsState().value.enabledChangeClient ?: true

    var expandedClients by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
            .padding(4.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dodaj Płatność",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        // TYTUŁ
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = payment?.title ?: "",
                onValueChange = { viewModel.updatePaymentState(payment?.copy(title = it)) },
                label = { Text("Tytułem:") },
                placeholder = { Text("") },
            )
        }
        // KLIENT
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text("Klient: ",
                modifier = Modifier.padding(4.dp))
            ClientsDropdown(
                chosenClient = client,
                selectClient = { selectedClient ->
                    viewModel.fetchClientForPayment(selectedClient)
                },
                clients = clients,
                expanded = expandedClients,
                toggleExpanded = { expandedClients = !expandedClients },
                enabled = changeClient,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        // KWOTA wpłaty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = payment?.amount.toString(),
                    onValueChange = {
                        if(it.isBlank()) viewModel.updatePaymentState(payment?.copy(amount = 0.00) )
                        if (it.isDigitsOnly()) viewModel.updatePaymentState(payment?.copy(amount = it.toDoublePl() ))
                        },
                    label = { Text("kwota brutto:") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("") },
                    modifier = Modifier
                )
            }
        // DATA wpłaty
        Row(
            modifier = Modifier
                .padding(4.dp)
        ) {
            ChooseDate(
                title = "Data płatności:",
                date = payment?.date ?: LocalDate.now(),
                updateDate = {
                    viewModel.updatePaymentState(payment?.copy(date = LocalDate.parse(it)))
                },
                modifier = Modifier.padding(4.dp)
            )
        }
        // METODA
        MethodDropdownMenu(
            payment?.method,
            { viewModel.updatePaymentState(payment?.copy(method = it)) }
        )
        // NA KONTO
        if(payment?.method == PaymentMethod.TRANSFER.name) {
            Row {
                OutlinedTextField(
                    value = "",
                    onValueChange = {

                    },
                    label = { Text("Na Konto:") },
                    placeholder = { Text("") },
                )
            }
        }
        // ZA FAKTURĘ
//        ForInvoicePayment(viewModel)
        HorizontalDivider()
        // NOWA / DODAJ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton({
                viewModel.newPaymentState()
            }) {
                Text("Nowa Płatność")
            }
            TextButton({
                if(payment != null) viewModel.postPaymentToApiWithResponse(payment)
                val clientId: Long? = client?.id
                if(clientId != null) {
                    viewModel.fetchPaymentsForClient(client.id!!)
                    viewModel.toPaymentsMenu()
                }

            }) { Text("Dodaj Płatność") }
        }
    }
}

@Composable
fun MethodDropdownMenu(method: String?, onClick: (String) -> Unit) {
    var expandedMethod by remember { mutableStateOf(false) }
    val method = if(method != null) PaymentMethod.valueOf(method).polishName else "Wybierz metodę:"

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = method,
            modifier = Modifier
                .clickable { expandedMethod = true }
                .padding(8.dp)
        )

        DropdownMenu(
            expanded = expandedMethod,
            onDismissRequest = { expandedMethod = false }
        ) {
            PaymentMethod.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.polishName) },
                    onClick = { onClick(item.name) }
                )
            }
        }
    }
}
// TODO Zrobić listę invoiców nieopłaconych, za które można chcieć wpłacić
//@Composable
//fun ForInvoicePayment(viewModel: ParkingAppViewModel) {
//    val payment: Payment? = viewModel.state.collectAsState().value.payment
//    val invoices: List<Invoice> = payment?.forInvoices ?: emptyList()
//    val invoicesList: List<String> = invoices.mapNotNull { it.invoiceNumber }
//
//    Column {
//        Row {
//            OutlinedTextField(
//                value = "",
//                onValueChange = {
//
//                },
//                label = { Text("Za fakturę numer:") },
//                placeholder = { Text("") },
//            )
//            IconButton( {
//
//                viewModel.updatePaymentState(payment?.copy(forInvoices = ))
//            } ) {
//                Icon(Icons.Default.Add, "dodaj")
//            }
//        }
//        invoices.forEach {
//            Row {
//                Text(it)
//                IconButton( {} ) {
//                    Icon(Icons.Default.Clear, "usuń")
//                }
//            }
//        }
//    }
//}

//@Preview(showBackground = true, widthDp = 300 )
//@Composable
//fun PaymentFormPreview() {
//    val viewModel = ParkingAppViewModel()
//    PaymentForm(viewModel)
//}