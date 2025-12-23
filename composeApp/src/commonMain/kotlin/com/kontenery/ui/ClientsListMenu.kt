package com.kontenery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.model.ClientOnList
import com.kontenery.model.enums.ClientFilter
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate
import kotlin.enums.EnumEntries

@Composable
fun ClientTable(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    ClientsListWithFilter(viewModel, modifier)
}

@Composable
fun ClientsListWithFilter(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val clients: List<ClientOnList> = viewModel.state.collectAsState().value.clients
    var selectedFilter by remember { mutableStateOf(ClientFilter.ALL) }
    var query by remember { mutableStateOf("") }

    val filteredClients = remember(query, clients, selectedFilter) {
        clients.filter { it.name.contains(query, ignoreCase = true) }
    }
    val sortedClients = when (selectedFilter) {
        ClientFilter.ALL -> filteredClients.sortedWith(
            compareByDescending<ClientOnList> { it.active }
                .thenBy { it.name }
        )
        ClientFilter.INACTIVE -> filteredClients.sortedWith(
            compareBy<ClientOnList> { it.active }
                .thenBy { it.name }
        )
        ClientFilter.OVERDUE -> filteredClients.sortedWith(
            compareByDescending<ClientOnList> { it.active }
                .thenBy { it.paymentsOverdue ?: 0.00 }
                .thenBy { it.name }
        )
        ClientFilter.OVERPAID -> filteredClients.sortedWith(
            compareByDescending<ClientOnList> { it.active }
                .thenByDescending { it.paymentsOverdue ?: 0.00 }
                .thenBy { it.name }
        )
        ClientFilter.INVOICE -> filteredClients.filter { it.invoice }
            .sortedWith(compareByDescending<ClientOnList> { it.active }
                    .thenBy { it.name }
            )
        ClientFilter.BILL -> filteredClients.filterNot { it.invoice }
            .sortedWith(compareByDescending<ClientOnList> { it.active }
                    .thenBy { it.name }
            )
        ClientFilter.NOCONTRACT -> filteredClients.filter { it.contracts.isNullOrEmpty() }
            .sortedWith(compareByDescending<ClientOnList> { it.active }
                .thenBy { it.name }
            )
    }
    if(clients.isEmpty()) LoadingBox("listę klientów")
    else Column(modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Szukaj klienta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        FilterButtons(
            filters = ClientFilter.entries,
            selectedFilter = selectedFilter,
            changeFilter = { selectedFilter = it },
            labelProvider = { it.label }
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sortedClients) { client ->
                ClientListItem(viewModel, client)
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

@Composable
fun ClientListItem(
    viewModel: ParkingAppViewModel,
    client: ClientOnList,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val backgroundColor = if (client.contracts.isNullOrEmpty()) Color.LightGray.copy(alpha = 0.2f) else Color.Transparent

    ListItem(
        modifier = Modifier.alpha(if (client.contracts.isNullOrEmpty()) 0.5f else 1f)
            .background(backgroundColor),
        headlineContent = {
            TextButton(
                onClick = { viewModel.toggleClientNavRow(client.id) },
                modifier = Modifier.padding(0.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    client.name,
                    Modifier.fillMaxHeight().padding(0.dp)
                )
            }

        },
        supportingContent = {
            val annotated = buildAnnotatedString {
                append("Umowy: ${if (client.contracts.isNullOrEmpty()) "brak" else client.contracts.joinToString()}")

                client.paymentsOverdue?.let { overdue ->
                    append("\n")
                    // jeśli zaległość ujemna, kolor tła będzie czerwony
                    if (overdue < 0.00) {
                        append("Zaległości: ")
                        pushStyle(SpanStyle(background = Color(0xFFB00020), color = Color.White))
                    } else if(overdue > 0.00) {
                        append("Płatności: ")
                        pushStyle(SpanStyle(color = Color(0xFF2E7D32)))
                    } else {
//                        println("Overdue: ", "$overdue")
                        append("Nadpłata: ")
                        pushStyle(SpanStyle(background = Color(0xFFFFA500), color = Color.White))
                    }
                    append(" $overdue ")
                    pop()
                }

                // Kolorujemy ostatnią fakturę warunkowo
                val lastInvoiceText = client.lastBill ?: "nie wysłana faktura/rachunek"
                val color = if (checkLastInvoiceSend(client)) Color(0xFF2E7D32) else Color(0xFFB00020)
                append("\n")
                pushStyle(SpanStyle(color = color))
                append("Ostatnia faktura: $lastInvoiceText")
                pop()
            }
            Text(
                text = annotated,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )
            Text(buildString {
                append("Umowy: ${if(client.contracts.isNullOrEmpty()) "brak" else client.contracts.joinToString()}")
                if (client.paymentsOverdue != null) append("\nZaległości: ${client.paymentsOverdue}")
                if (client.lastBill != null) append("\nOstatnia faktura: ${client.lastBill}")
            })
        },
        trailingContent = {
            if (client.active) {
                AssistChip(onClick = {}, label = { Text("Aktywny") })
            } else {
                AssistChip(
                    onClick = {},
                    label = { Text("Nieaktywny") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.LightGray),
                )
            }
        }
    )
    AnimatedVisibility(
        visible = client.id == state.clientNavRow,
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {
        ClientNavRow(client.id, viewModel, modifier)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T : Enum<T>> FilterButtons(
    filters: EnumEntries<T>,
    selectedFilter: T,
    changeFilter: (T) -> Unit,
    labelProvider: (T) -> String
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { changeFilter(filter) },
                label = { Text(labelProvider(filter)) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientNavRow(clientId: Long, viewModel: ParkingAppViewModel, modifier: Modifier = Modifier) {
    FlowRow(modifier = modifier
            .fillMaxWidth()
        , verticalArrangement = Arrangement.Center
        , horizontalArrangement = Arrangement.SpaceAround)
    {
        // Dane klienta
        TextButton(onClick = { viewModel.toClientData(clientId) }
            , modifier = modifier
                .padding(end = 4.dp)
                .wrapContentSize()
            , border = ButtonDefaults.outlinedButtonBorder()
            , contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            , shape = RoundedCornerShape(25)
        ) {
            Text("Dane klienta",
                modifier = modifier
                    .wrapContentSize(align = Alignment.Center)
                , textAlign = TextAlign.Center
            )
        }

        // faktury i wplaty
        TextButton(
            onClick = {
                viewModel.updateClient(clientId)
                viewModel.fetchPaymentsForClient(clientId)
                viewModel.fetchInvoicesForClient(clientId)
                viewModel.toPaymentsMenu()
            }
            , modifier = modifier
                .padding(end = 4.dp)
                .wrapContentSize()
            , border = ButtonDefaults.outlinedButtonBorder()
            , contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            , shape = RoundedCornerShape(25)
        ) {
            Text("Płatności",
                modifier = modifier
                , textAlign = TextAlign.Center
            )
        }

        // Umowy z klientem
        TextButton(onClick = {
            viewModel.updateClient(clientId)
            viewModel.fetchContractsForClient(clientId)
            viewModel.toContractList()
        }
            , modifier = modifier
                .padding(end = 4.dp)
                .wrapContentSize()
            , border = ButtonDefaults.outlinedButtonBorder()
            , contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            , shape = RoundedCornerShape(25)
        ) {
            Text("Umowy",
                modifier = modifier
                , textAlign = TextAlign.Center
            )
        }

        // wyslij fakturę
        TextButton(
            onClick = {
                viewModel.sendPeriodicInvoice(clientId)
            }
            , modifier = modifier
                .padding(end = 4.dp)
                .wrapContentSize()
            , border = ButtonDefaults.outlinedButtonBorder()
            , contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            , shape = RoundedCornerShape(25)
        ) {
            Text("wyślij fakturę",
                modifier = modifier
                , textAlign = TextAlign.Center
            )
        }

        // przypomnij o fakturze
        TextButton(onClick = {
            // TODO znajdź fakturę niopłaconą i wyślij przypomnienie
//            viewModel.postPeriodicInvoiceAgain(invoice.invoiceNumber!!)
        }
            , modifier = modifier
                .wrapContentSize()
            , border = ButtonDefaults.outlinedButtonBorder()
            , contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            , shape = RoundedCornerShape(25)
        ) {
            Text(
                "Przypomnij",
                modifier = modifier, textAlign = TextAlign.Center
            )
        }
    }
}

private fun checkLastInvoiceSend(client: ClientOnList): Boolean {
    val currentDate: LocalDate = LocalDate.now()
    val lastBill: LocalDate = client.lastBill ?: return false
    return currentDate.monthNumber <= lastBill.monthNumber
}

//@Preview()
//@Composable
//fun PreviewClientsListWithFilter() {
//    val demoClients = listOf(
//        ClientOnList(1, "Jan Kowalski",
//            123.45, listOf("a1, a2, -B2"), true, true, LocalDate.now().minus(5, DateTimeUnit.DAY)),
//        ClientOnList(2, "Anna Nowak", null, null, false, false, null),
//        ClientOnList(3, "Firma XYZ", 0.00, listOf("c1, C2, C3"), true, true, LocalDate.now().minus(1, DateTimeUnit.DAY)),
//        ClientOnList(4, "Firma minusowa", -100.00, listOf("d1, D2, D3"), true, true, LocalDate.now().minus(1, DateTimeUnit.DAY))
//    )
//    val viewModel = ParkingAppViewModel()
//    val state = ParkingAppState()
//    state.copy(clients = demoClients)
//    viewModel.setState(state.copy(clients = demoClients))
//
//    MaterialTheme {
//        ClientsListWithFilter(viewModel)
//    }
//}