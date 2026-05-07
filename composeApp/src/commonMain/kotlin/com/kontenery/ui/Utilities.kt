package com.kontenery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.model.Submeter
import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.enums.UtilityIcon
import com.kontenery.model.enums.UtilityType
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.ParkingAppViewModel
@Composable
fun Utilities(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val submeters = state.submeters

    var expandedSubmeter by remember { mutableStateOf<Submeter?>(null) }

    Box(modifier) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(submeters) { submeter ->
                SubmeterCard(
                    submeter = submeter,
                    onAddReading = {
                        viewModel.createNewInvoice(InvoiceType.UTILITIES)
                        viewModel.toAddInvoice()
                        it.clientId?.let(viewModel::updateCustomerToInvoice)
                    },
                    onDetails = {
                        it.id?.let(viewModel::fetchSubmeter)
                        viewModel.toReadings()
                    }
                )
            }
        }

        SubmeterMenu(
            expandedSubmeter = expandedSubmeter,
            onDismiss = { expandedSubmeter = null },
            viewModel = viewModel
        )

        FloatingActionButton(
            onClick = { viewModel.toSubmeter() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+")
        }
    }
}

@Composable
fun SubmeterCard(
    submeter: Submeter,
    onAddReading: (Submeter) -> Unit,
    onDetails: (Submeter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Lokalizacja: ${submeter.location}")

                    Row {
                        submeter.utilityType?.let { UtilityIcon(it) }
                        Text(submeter.number ?: "")
                    }

                    Text("Odczytów: ${submeter.readings.size}")
                    Text("Klient: ${submeter.clientId}")
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Wystaw fakturę") },
                            onClick = {
                                onAddReading(submeter)
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Dane podlicznika") },
                            onClick = {
                                onDetails(submeter)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubmeterMenu(
    expandedSubmeter: Submeter?,
    onDismiss: () -> Unit,
    viewModel: ParkingAppViewModel
) {
    DropdownMenu(
        expanded = expandedSubmeter != null,
        onDismissRequest = onDismiss
    ) {
        val submeter = expandedSubmeter ?: return@DropdownMenu

        DropdownMenuItem(
            text = { Text("Dodaj odczyt") },
            onClick = {
                viewModel.createNewInvoice(InvoiceType.UTILITIES)
                viewModel.toAddInvoice()
                submeter.clientId?.let {
                    viewModel.updateCustomerToInvoice(it)
                }
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = { Text("Dane podlicznika") },
            onClick = {
                submeter.id?.let { viewModel.fetchSubmeter(it) }
                viewModel.toReadings()
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubmeterScreen(
    widthSizeClass: WindowWidthSizeClass,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {

    var clientId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var utilityType by remember { mutableStateOf<UtilityType?>(null) }

    var expanded by remember { mutableStateOf(false) }

    val isValid = location.isNotBlank()
            && number.isNotBlank()
            && utilityType != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Dodawanie podlicznika",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID (opcjonalnie)") },
            singleLine = true
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Lokalizacja") },
            singleLine = true
        )

        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Numer licznika") },
            singleLine = true
        )

        // 🔽 Dropdown UtilityType
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = utilityType?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Typ mediów") },
                modifier = Modifier.menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                UtilityType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            utilityType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val submeter = Submeter(
                        clientId = clientId.toLongOrNull(),
                        location = location,
                        number = number,
                        utilityType = utilityType,
                        readings = emptyList()
                    )
                    viewModel.postSubmeter(submeter)
                },
                enabled = isValid
            ) {
                Text("Dodaj Podlicznik")
            }

            OutlinedButton(onClick = {
                clientId = ""
                location = ""
                number = ""
                utilityType = null
                viewModel.toUtility()
            }) {
                Text("Anuluj")
            }
        }

        if (!isValid) {
            Text(
                text = "Uzupełnij wymagane pola (lokalizacja, numer, typ)",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}