package com.kontenery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.model.Submeter
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.enums.UtilityIcon
import com.kontenery.service.ParkingAppViewModel

@Composable
fun Utilities(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val submeters: List<Submeter> = state.submeters
    var expandedSubmeterId by remember { mutableStateOf<Long?>(null) }
//    var expandedSubmeter by remember { mutableStateOf<Submeter?>(null) }
    var showDialogForSubmeterId by remember { mutableStateOf<Long?>(null) }
    var newReading by remember { mutableStateOf("") }

    Box(modifier) {

    LazyColumn {
        items(submeters) { submeter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { expandedSubmeterId = submeter.id },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Lokalizacja: ${submeter.location}")
                    Row {
                        UtilityIcon(submeter.utilityType)
                        Text("${submeter.number}")
                    }
                    Text(text = "Odczytów: ${submeter.readings.size}")
                    Text("Klient: ${submeter.clientId}")
                    ClientNameLoader(submeter.clientId, { id -> viewModel.getClientNameById(id!!) })
                }

                // Menu po kliknięciu
                DropdownMenu(
                    expanded = expandedSubmeterId == submeter.id,
                    onDismissRequest = { expandedSubmeterId = null }
                ) {
                    DropdownMenuItem(
                        text = { Text("Dodaj odczyt") },
                        onClick = {
                            viewModel.createNewInvoice(InvoiceType.UTILITIES)
                            viewModel.toAddInvoice()
                            if(submeter.clientId != null) viewModel.updateCustomerToInvoice(submeter.clientId)
                            // TODO przenieś do wystawienia faktury dla danego klienta!!!
                            expandedSubmeterId = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Dane podlicznika") },
                        onClick = {
                            if(submeter.id != null) viewModel.fetchSubmeter(submeter.id)
                            viewModel.toReadings()
                            expandedSubmeterId = null
                        }
                    )
                }
            }
        }
    }

    // Dialog do dodania nowego odczytu

    }
}
@Composable
fun ClientNameLoader(
    clientId: Long?,
    getClientName: (Long?) -> String?,
    modifier: Modifier = Modifier
) {
    if(clientId == null) Text("Licznik nieprzypisany")
    else {
        val name = getClientName(clientId) ?: "Brak nazwy klienta dla id: $clientId"
        Text("Klient: $name", modifier)
    }
}