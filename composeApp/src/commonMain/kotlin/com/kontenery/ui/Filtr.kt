package com.example.parkingandroidview.ui

//
//@Composable
//fun ClientsListWithFilter(
//    viewModel: ParkingAppViewModel,
//    modifier: Modifier = Modifier
//) {
//    val clients = viewModel.state.collectAsState().value.clients
//    var query by remember { mutableStateOf("") }
//
//    val filteredClients = remember(query, clients) {
//        clients.filter { it.name.contains(query, ignoreCase = true) }
//    }
//
//    Column(modifier.fillMaxSize().padding(16.dp)) {
//        OutlinedTextField(
//            value = query,
//            onValueChange = { query = it },
//            label = { Text("Szukaj klienta") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//
//
//        Spacer(Modifier.height(12.dp))
//
//        LazyColumn(modifier = Modifier.fillMaxSize()) {
//            items(filteredClients) { client ->
//                ClientListItem(viewModel, client)
//                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
//            }
//        }
//    }
//}
//
//@Composable
//fun ClientListItem(
//    viewModel: ParkingAppViewModel,
//    client: ClientOnList,
//    modifier: Modifier = Modifier
//) {
//    val state by viewModel.state.collectAsState()
//
//    ListItem(
//        headlineContent = {
//            TextButton(
//                onClick = {viewModel.toggleClientNavRow(client.id)},
//                modifier = Modifier.padding(0.dp),
//                contentPadding = PaddingValues(0.dp)
//            ) {
//                Text(
//                    client.name,
//                    Modifier.fillMaxHeight().padding(0.dp)
//                )
//            }
//
//        },
//        supportingContent = {
//            Text(buildString {
//                append("Umowy: ${if(client.contracts.isNullOrEmpty()) "brak" else client.contracts?.joinToString()}")
//                if (client.paymentsOverdue != null) append("\nZaległości: ${client.paymentsOverdue}")
//                if (client.lastBill != null) append("\nOstatnia faktura: ${client.lastBill}")
//            })
//        },
//        trailingContent = {
//            if (client.active) {
//                AssistChip(onClick = {}, label = { Text("Aktywny") })
//            } else {
//                AssistChip(onClick = {}, label = { Text("Nieaktywny") })
//            }
//        }
//    )
//    AnimatedVisibility(
//        visible = client.id == state.clientNavRow,
//        modifier = Modifier.padding(horizontal = 5.dp)
//    ) {
//        ClientNavRow(client.id, viewModel, modifier)
//    }
//}


//
//
//import kotlinx.datetime.LocalDate
//import java.math.BigDecimal
//
//// 🔹 Twoja klasa
//@kotlinx.serialization.Serializable
//data class ClientOnList(
//    val id: Long,
//    val name: String,
//    val paymentsOverdue: BigDecimal?,
//    val contracts: String?,
//    val active: Boolean,
//    val invoice: Boolean,
//    val lastBill: LocalDate?
//)
//
//// 🔹 Kategorie filtrów
//enum class ClientFilter(val label: String) {
//    ALL("Wszyscy"),
//    ACTIVE("Aktywni"),
//    OVERDUE("Zaległości"),
//    INVOICE("Faktury")
//}

//@Composable
//fun ClientListScreen(clients: List<ClientOnList>) {
//    var selectedFilter by remember { mutableStateOf(ClientFilter.ALL) }
//    var searchQuery by remember { mutableStateOf("") }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        // 🔹 Filtry
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            ClientFilter.entries.forEach { filter ->
//                FilterChip(
//                    selected = selectedFilter == filter,
//                    onClick = { selectedFilter = filter },
//                    label = { Text(filter.label) }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // 🔹 Wyszukiwarka
//        BasicTextField(
//            value = searchQuery,
//            onValueChange = { searchQuery = it },
//            singleLine = true,
//            textStyle = TextStyle(color = Color.Black),
//            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
//                .padding(horizontal = 12.dp, vertical = 8.dp)
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // 🔹 Filtrowanie
//        val filteredClients = clients.filter { client ->
//            val matchesSearch = client.name.contains(searchQuery, ignoreCase = true)
//            val matchesFilter = when (selectedFilter) {
//                ClientFilter.ALL -> true
//                ClientFilter.ACTIVE -> client.active
//                ClientFilter.OVERDUE -> (client.paymentsOverdue ?: BigDecimal.ZERO) > BigDecimal.ZERO
//                ClientFilter.INVOICE -> client.invoice
//            }
//            matchesSearch && matchesFilter
//        }
//
//        // 🔹 Lista
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            items(filteredClients.size) { index ->
//                val client = filteredClients[index]
//                ClientItem(client)
//            }
//        }
//    }
//}
//
//@Composable
//fun ClientItem(client: ClientOnList) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = client.name, style = MaterialTheme.typography.titleMedium)
//            if (client.paymentsOverdue != null && client.paymentsOverdue > BigDecimal.ZERO) {
//                Text(text = "Zaległość: ${client.paymentsOverdue} PLN", color = Color.Red)
//            }
//            Text(text = "Aktywny: ${if (client.active) "Tak" else "Nie"}")
//            Text(text = "Faktura: ${if (client.invoice) "Tak" else "Nie"}")
//        }
//    }
//}
//
//// 🔹 Podgląd
//@Preview(showBackground = true)
//@Composable
//fun PreviewClientListScreen() {
//    val sampleClients = listOf(
//        ClientOnList(1, "Jan Kowalski", BigDecimal("200.50"), "Umowa A", true, true, null),
//        ClientOnList(2, "Anna Nowak", BigDecimal.ZERO, "Umowa B", true, false, null),
//        ClientOnList(3, "Marek Wiśniewski", BigDecimal("50.00"), "Umowa C", false, true, null),
//    )
//    MaterialTheme {
//        ClientListScreen(clients = sampleClients)
//    }
//}