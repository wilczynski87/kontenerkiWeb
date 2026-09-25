package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.kontenery.model.Client
import com.kontenery.model.ClientOnList
import com.kontenery.model.PaymentDto
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.util.formatLocalDate
import com.kontenery.util.isDigitsOnly
import com.kontenery.util.to2Decimals
import com.kontenery.util.toDoublePl
import kotlinx.datetime.LocalDate

@Composable
fun PaymentTransferForm(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val payment: PaymentDto? = state.payment
    val sourceClient: Client? = state.client
    val targetClient: Client? = state.transferTargetClient
    val clients: List<ClientOnList> = state.clients
    if (clients.isEmpty()) viewModel.getClientsList(0, 1000)

    var expandedClients by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Zaliczenie płatności",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            Text(
                text = "Z płatności:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp),
            )
            Text(
                text = "ID: ${payment?.paymentId ?: "-"}",
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Text(
                text = "Klient: ${sourceClient?.getName() ?: payment?.fromClientId ?: "-"}",
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Text(
                text = "Data: ${payment?.date?.let(::formatLocalDate) ?: "-"}",
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Text(
                text = "Kwota źródłowa: ${payment?.amount?.to2Decimals() ?: "-"} zł",
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            )
        }

        Text(
            text = "Na klienta:",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelLarge,
        )
        ClientsListDropdown(
            client = targetClient,
            clients = clients.filter { it.id != payment?.fromClientId },
            expanded = expandedClients,
            onExpandedChange = { expandedClients = !expandedClients },
            onSelect = { selectedClientId ->
                viewModel.fetchClientForPaymentTransfer(selectedClientId)
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            OutlinedTextField(
                value = payment?.amount?.toString() ?: "",
                onValueChange = {
                    if (it.isBlank()) {
                        viewModel.updatePaymentState(payment?.copy(amount = 0.00))
                    } else if (it.isDigitsOnly()) {
                        viewModel.updatePaymentState(payment?.copy(amount = it.toDoublePl()))
                    }
                },
                label = { Text("Kwota zaliczenia:") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        Row(
            modifier = Modifier.padding(4.dp),
        ) {
            ChooseDate(
                title = "Data zaliczenia:",
                date = payment?.date ?: LocalDate.now(),
                updateDate = {
                    viewModel.updatePaymentState(payment?.copy(date = LocalDate.parse(it)))
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            OutlinedTextField(
                value = payment?.title ?: "",
                onValueChange = { viewModel.updatePaymentState(payment?.copy(title = it)) },
                label = { Text("Tytuł (opcjonalnie):") },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextButton({ viewModel.toPaymentsMenu() }) {
                Text("Anuluj")
            }
            TextButton({
                viewModel.transferPaymentAndRefresh()
            }) {
                Text("Wykonaj zaliczenie")
            }
        }
    }
}
