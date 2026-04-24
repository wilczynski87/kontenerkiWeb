package com.example.parkingandroidview.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import com.kontenery.service.ParkingAppViewModel

@Composable
fun Utilites(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val submeters: List<Submeter> = state.submeters
    var expandedSubmeterId by remember { mutableStateOf<Long?>(null) }
    var showDialogForSubmeterId by remember { mutableStateOf<Long?>(null) }
    var newReading by remember { mutableStateOf("") }

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
                    Text(text = "Typ: ${submeter.utilityType?.polishName}")
                    Text(text = "Odczytów: ${submeter.readings.size}")
                }

                // Menu po kliknięciu
                DropdownMenu(
                    expanded = expandedSubmeterId == submeter.id,
                    onDismissRequest = { expandedSubmeterId = null }
                ) {
                    DropdownMenuItem(
                        text = { Text("Dodaj odczyt") },
                        onClick = {
                            expandedSubmeterId = null
                            showDialogForSubmeterId = submeter.id
                        }
                    )
                }
            }
        }
    }

    // Dialog do dodania nowego odczytu
    if (showDialogForSubmeterId != null) {
        AlertDialog(
            onDismissRequest = { showDialogForSubmeterId = null },
            title = { Text("Nowy odczyt") },
            text = {
                OutlinedTextField(
                    value = newReading,
                    onValueChange = { newReading = it },
                    label = { Text("Odczyt") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialogForSubmeterId?.let { id ->
//                        onAddReading(id, newReading)
                    }
                    newReading = ""
                    showDialogForSubmeterId = null
                }) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogForSubmeterId = null }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SubmeterScreen() {
//    val submeters = listOf(
//        Submeter(
//            id = 1,
//            clientId = 101,
//            location = "Piwnica",
//            utilityType = UtilityType.WATER,
//            readings = listOf(
//                Reading(1, 1, UtilityType.WATER, "123.4", LocalDate.now().minus(10, DateTimeUnit.DAY), BigDecimal("5.20")),
//                Reading(2, 1, UtilityType.WATER, "127.8", LocalDate.now(), BigDecimal("5.30"))
//            )
//        ),
//        Submeter(
//            id = 2,
//            clientId = 102,
//            location = "Kuchnia",
//            utilityType = UtilityType.ELECTRICITY,
//            readings = listOf(
//                Reading(3, 2, UtilityType.ELECTRICITY, "3200", LocalDate.now().minus(15, DateTimeUnit.DAY), BigDecimal("0.85")),
//                Reading(4, 2, UtilityType.ELECTRICITY, "3400", LocalDate.now(), BigDecimal("0.90"))
//            )
//        ),
//        Submeter(
//            id = 3,
//            clientId = 103,
//            location = "Garaż",
//            utilityType = UtilityType.ELECTRICITY,
//            readings = listOf(
//                Reading(5, 3, UtilityType.ELECTRICITY, "56.7", LocalDate.now().minus(20, DateTimeUnit.DAY), BigDecimal("3.15")),
//                Reading(6, 3, UtilityType.ELECTRICITY, "63.1", LocalDate.now(), BigDecimal("3.20"))
//            )
//        ),
//        Submeter(
//            id = 4,
//            clientId = 104,
//            location = "Łazienka",
//            utilityType = UtilityType.WATER,
//            readings = listOf(
//                Reading(
//                    7,
//                    4,
//                    UtilityType.WATER,
//                    "44.0",
//                    LocalDate.now().minus(5, DateTimeUnit.DAY),
//                    BigDecimal("5.10")
//                )
//            )
//        )
//    )
//    val viewModel = ParkingAppViewModel()
//    val state = ParkingAppState(submeters = submeters)
//    viewModel.setState(state)
//
//    Utilites(
//        viewModel
//    )
//}