package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.model.Reading
import com.kontenery.model.Submeter
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import com.kontenery.controller.ApiClients
import com.kontenery.model.Client
import com.kontenery.model.ClientOnList
import com.kontenery.model.enums.UtilityType
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate

@Composable
fun ReadingsView(
    windowWidthSizeClass: WindowWidthSizeClass,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val submeter = state.submeter ?: Submeter()
    val client = state.client
    val clientOnList = state.clients

    println("UI clientId = ${submeter.clientId}")

    Column(modifier = modifier) {
        SubmeterReadingsView(
            submeter,
            client,
            clientOnList,
            viewModel,
            { submeterId, reading -> viewModel.postSubmeterReading(submeterId, reading) },
            { submeterId, readingId -> viewModel.deleteReading(submeterId, readingId) },
            {
                viewModel.fetchSubmeters()
                viewModel.toUtility()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun SubmeterReadingsView(
    submeter: Submeter,
    client: Client? = null,
    clients: List<ClientOnList> = listOf(),
    viewModel: ParkingAppViewModel,
    onAddReading: (Long, Reading) -> Unit,
    onDeleteReading: (Long, Long) -> Unit,
    onBack: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var expandedClients by remember { mutableStateOf(false) }
//    val clientName = if(submeter.clientId == null ) "brak" else viewModel.getClientNameById(submeter.clientId)
    val clientName = if(submeter.clientId == null ) "brak" else clients
        .firstOrNull { it.id == submeter.clientId }
        ?.name ?: "brak"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Podlicznik ${submeter.number ?: "-"}")

                        Text(
                            text = "Najemca: $clientName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Zmień właściciela") },
                            onClick = {
                                showAssignDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Dodaj odczyt"
                )
            }
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {
            if(showAssignDialog)
                ClientsListDropdown(
                    client = client,
                    clients = clients,
                    modifier = Modifier.padding(4.dp),
                    expanded = expandedClients,
                    onExpandedChange = { expandedClients = it },
                    enabled = true,
                    onSelect = { clientId ->
                        showAssignDialog = false
                        println(" submenter after change: ${submeter.copy(clientId = clientId)}")
                        if(submeter.id != null) viewModel.updateSubmeterClient(clientId, submeter.copy(clientId = clientId))
                    },
                )

            LazyColumn(
                modifier = Modifier
//                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(submeter.readings.sortedByDescending { it.date }) { reading ->
                    ReadingCard(
                        reading = reading,
                        onDelete = { if(submeter.id != null && reading.id != null) onDeleteReading( submeter.id, reading.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddReadingDialog(
                submeter = submeter,
                utilityType = submeter.utilityType,
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    onAddReading(submeter.id!!, it)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ReadingCard(
    reading: Reading,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = reading.reading.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = reading.date.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń"
                )
            }
        }
    }
}


@Composable
fun ReadingRow(
    reading: Reading,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text("Odczyt: ${reading.reading ?: "-"}")
                Text("Data: ${reading.date ?: "-"}")
                Text("Cena netto: ${reading.currentUnitPriceNet ?: "-"}")
            }

            Spacer(modifier = Modifier.padding(4.dp))

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń")
            }
        }
    }
}

@Composable
fun AddReadingDialog(
    submeter: Submeter,
    utilityType: UtilityType?,
    onDismiss: () -> Unit,
    onConfirm: (Reading) -> Unit
) {
    val lastReading = submeter.readings
        .filter { it.date != null }
        .maxByOrNull { it.date!! }

    val suggestedPrice = lastReading?.currentUnitPriceNet?.toString()

    var value by remember(submeter.id) { mutableStateOf("") }
    var price by remember(submeter.id) { mutableStateOf(suggestedPrice ?: "") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }

    var valueError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val priceRegex = Regex("""^\d+(\.\d{0,2})?$""")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedValue = value.toDoubleOrNull()
                    val parsedPrice = price.toDoubleOrNull()
                    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()

                    valueError = parsedValue == null
                    priceError = parsedPrice == null || !priceRegex.matches(price)
                    dateError = parsedDate == null

                    if (valueError || priceError || dateError) return@Button

                    onConfirm(
                        Reading(
                            submeterId = submeter.id,
                            utilityType = utilityType,
                            reading = parsedValue.toString(),
                            currentUnitPriceNet = parsedPrice,
                            date = parsedDate
                        )
                    )
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        },
        title = { Text("Nowy odczyt") },
        text = {
            Column {

                // 🔵 ODCZYT
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        val parsed = it.toDoubleOrNull()

                        valueError = when {
                            it.isBlank() -> true
                            parsed == null -> true
                            parsed <= (lastReading?.reading?.toDoubleOrNull() ?: 0.00) -> true
                            else -> false
                        }
                    },
                    isError = valueError,
                    label = { Text("Odczyt") },
                    supportingText = {
                        if (valueError) Text("Wpisz poprawną liczbę")
                    },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                // 🟡 CENA
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = it.isNotBlank() && !priceRegex.matches(it)
                    },
                    isError = priceError,
                    label = { Text("Cena netto") },
                    supportingText = {
                        if (priceError) Text("Format: 123 lub 123.45")
                    },
                    placeholder = {
                        Text(suggestedPrice ?: "")
                    },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                // 🟢 DATA
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        dateError = runCatching { LocalDate.parse(it) }.isFailure
                    },
                    isError = dateError,
                    label = { Text("Data (YYYY-MM-DD)") },
                    supportingText = {
                        if (dateError) Text("Format: YYYY-MM-DD")
                    },
                    singleLine = true
                )
            }
        }
    )
}