package com.kontenery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kontenery.model.Client
import com.kontenery.model.ClientOnList
import com.kontenery.model.Reading
import com.kontenery.model.Submeter
import com.kontenery.model.enums.UtilityIcon
import com.kontenery.model.enums.UtilityType
import com.kontenery.model.enums.now
import com.kontenery.model.invoice.Invoice
import com.kontenery.service.ParkingAppState
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.ui.SubmeterRow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmeterSelectionScreen(
    utilityType: UtilityType,
    submeters: List<Submeter>,
    onSubmeterSelected: (Submeter) -> Unit,
    onBack: () -> Unit
) {
    var selectedSubmeter by remember { mutableStateOf<Submeter?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wybierz podlicznik",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Nagłówek informacyjny
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (utilityType) {
                            UtilityType.ELECTRICITY -> "⚡"
                            UtilityType.WATER -> "💧"
                        },
                        fontSize = 32.sp
                    )
                    Column {
                        Text(
                            text = "Wybrane media: ${utilityType.polishName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Wybierz podlicznik dla tego rodzaju mediów",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Lista podliczników
            if (submeters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📭",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "Brak podliczników",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Dla wybranego typu mediów nie znaleziono podliczników",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(submeters) { submeter ->
                        SubmeterCard(
                            submeter = submeter,
                            isSelected = selectedSubmeter == submeter,
                            onSelect = { selectedSubmeter = submeter }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        FilledTonalButton(
                            onClick = {
                                selectedSubmeter?.let { onSubmeterSelected(it) }
                            },
                            enabled = selectedSubmeter != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "Wybierz odczyt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmeterCard(
    submeter: Submeter,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onSelect,
                indication = ripple(
                    color = MaterialTheme.colorScheme.primary
                ),
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Podlicznik #${submeter.number ?: "BRAK"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    submeter.location?.let { location ->
                        Text(
                            text = "📍 $location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Wybrano",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Statystyki odczytów
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text("📊 ${submeter.readings.size} odczytów")
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )

                submeter.utilityType?.let { utility ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                when (utility) {
                                    UtilityType.ELECTRICITY -> "⚡ ${utility.polishName}"
                                    UtilityType.WATER -> "💧 ${utility.polishName}"
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            }

            // Ostatni odczyt
            // TODO to: LocalDate.now() trzeba zmienić na minimunm
            submeter.readings.maxByOrNull { it.date ?: LocalDate.now() }?.let { lastReading ->
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ostatni odczyt:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${lastReading.reading ?: "BRAK"} ${submeter.utilityType?.uom ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}



@Composable
fun UtilitiesChoice(
    viewModel: ParkingAppViewModel,
    state: ParkingAppState,
    modifier: Modifier = Modifier
) {
    val clients: List<ClientOnList> = state.clients
    val client: Client? = state.client
    val invoice: Invoice = state.invoice ?: return
    val submeters: List<Submeter> = state.submeters
    var expandedClients by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier.fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("Wystaw fakturę za media:")
        }
        Row {
            OutlinedCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Text("Klient: ",
                    modifier = Modifier.padding(4.dp))
                ClientsDropdown(
                    chosenClient = client,
                    selectClient = { selectedClient ->
                        viewModel.updateCustomerToInvoice(selectedClient)
                        viewModel.fetchSubmetersForClient(selectedClient)
//                        println(
//                            "ClientsDropdown selectedClient: $selectedClient, viewModel: $invoice"
//                        )
                    },
                    clients = clients,
                    expanded = expandedClients,
                    toggleExpanded = { expandedClients = !expandedClients },
                )
            }
        }
        Row() {
            BillType(
                needInvoice = client?.needInvoice() == true,
                enabled = false,
                toggleInvoice = { needInvoice: Boolean ->
                    viewModel.sellerForInvoiceUpdate()
                }
            )
        }
        Row() {
            ChooseDate(
                title = "Data wystawienia",
                date = LocalDate.now(),
                updateDate = { date ->
                    viewModel.updateInvoice(invoice.copy(invoiceDate = LocalDate.parse(date)))
                }
            )
        }
        Row() {
            OutlinedTextField(
                value = invoice.invoiceTitle ?: "",
                onValueChange = {
                    viewModel.updateInvoice(invoice.copy(invoiceTitle = it))
                },
                label = { Text("Tytuł faktury: ") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row {
            Button(
                onClick = {
                    // TODO jak nie ma id customera to jakiś błąd?
                    val invoice: Invoice = invoice
                    viewModel.postCustomInvoice(client?.id ?: 0, invoice)
                    viewModel.showConfirmModal(
                        "Status dodatkowej faktury",
                        "Wysyłam fakturę do klienta",
                        viewModel::closeConfirmationModal,
                    )
                },
            ) {
                Text("Wyślij dodatkową fakturę")
            }
        }

        ClientsUtilitiesReaders(viewModel, submeters)

    }
}

@Composable
fun ClientsUtilitiesReaders(
    viewModel: ParkingAppViewModel,
    submeters: List<Submeter> = listOf(),
    modifier: Modifier = Modifier,
) {
    Row {
        SubmeterTable(viewModel, submeters)
    }
}

@Composable
fun SubmeterTable(
    viewModel: ParkingAppViewModel,
    submeters: List<Submeter> = listOf(),
    modifier: Modifier = Modifier,
) {
    val columnWeights = mapOf(
        "nr" to 2f,
        "type" to 1f,
        "last" to 1f,
        "reading" to 3f,
        "price" to 3f,
        "button" to 2f
    )

    Column {
        // Header
        Row(Modifier
            .widthIn(min = 500.dp)
            .padding(vertical = 8.dp),
        ) {
            Text("Nr", Modifier.weight(columnWeights["nr"]!!))
            Text("Typ", Modifier.weight(columnWeights["type"]!!))
            Text("Ostatni", Modifier.weight(columnWeights["last"]!!))
            Text("Nowy odczyt", Modifier.weight(columnWeights["reading"]!!))
            Text("Stawka", Modifier.weight(columnWeights["price"]!!))
            Spacer(Modifier.weight(columnWeights["button"]!!))
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Column {
            submeters.forEach { item ->
                SubmeterRow(
                    submeter = item,
                    addReading = { submeterId, reading -> viewModel.postSubmeterReading(submeterId, reading) },
                    columnWeights,
                )
            }
        }
    }
}

@Composable
fun SubmeterRow(
    submeter: Submeter,
    addReading: (Long, Reading) -> Unit,
    columnWeights: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val lastReading = submeter.readings.maxByOrNull { it.date ?: LocalDate.now() }
    var value by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .widthIn(min = 500.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(submeter.number ?: "-", modifier = Modifier.weight(columnWeights["nr"]!!))

        UtilityIcon(submeter.utilityType, modifier = Modifier.weight(columnWeights["type"]!!))

        Text(lastReading?.reading.toString() ?: "-", modifier = Modifier.weight(columnWeights["last"]!!))

        var readingError by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it

                val parsed = it.toDoubleOrNull()
                readingError = when {
                    it.isBlank() -> true
                    parsed == null -> true
                    parsed < (lastReading?.reading ?: 0.0) -> true
                    else -> false
                }
            },
            isError = readingError,
            modifier = Modifier
                .weight(columnWeights["reading"]!!)
                .padding(4.dp),
            singleLine = true,
            label = { Text("Odczyt") },
            supportingText = {
                if (readingError) Text("Wpisz poprawną liczbę")
            }
        )

        var priceError by remember { mutableStateOf(false) }
        val priceRegex = Regex("""^\d+(\.\d{0,2})?$""")

        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
                priceError = it.isNotBlank() && !priceRegex.matches(it)
            },
            isError = priceError,
            supportingText = {
                if (priceError) Text("Format: 123 lub 123.45")
            },
            modifier = Modifier
                .weight(columnWeights["price"]!!)
                .padding(4.dp),
            singleLine = true,
            label = { Text("Cena") }
        )

        Button(
            onClick = {
                val id = submeter.id
                val readingValue = value.toDoubleOrNull()
                val priceValue = price.toDoubleOrNull()

                if (id == null) {
                    println("There is no submeter id!")
                    return@Button
                }

                if (readingValue == null || priceValue == null) {
                    println("Invalid input")
                    return@Button
                }
                addReading(
                    id,
                    Reading(
                        submeterId = id,
                        utilityType = submeter.utilityType,
                        reading = readingValue,
                        date = LocalDate.now(),
                        currentUnitPriceNet = priceValue
                    )
                )

                value = ""
                price = ""
            },
            modifier = Modifier
                .padding(start = 4.dp),
//                .weight(2f),
        ) {
            Text("Zapisz")
        }
    }
}