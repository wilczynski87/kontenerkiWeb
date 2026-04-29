package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
//    val submeter = state.submeter ?: return
    val submeter = state.submeter ?: Submeter()
    Column(modifier = modifier) {
        SubmeterReadingsView(
            submeter,
            {},
            {}
        )
    }
}

@Composable
fun SubmeterReadingsView(
    submeter: Submeter,
    onAddReading: (Reading) -> Unit,
    onDeleteReading: (Reading) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Trzeba wstawić powrót")

        Text(
            text = "Podlicznik: ${submeter.number ?: "-"}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = { showAddDialog = true }) {
            Text("Dodaj odczyt")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(submeter.readings.sortedByDescending { it.date }) { reading ->
                ReadingRow(
                    reading = reading,
                    onDelete = { onDeleteReading(reading) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddReadingDialog(
            submeterId = submeter.id,
            utilityType = submeter.utilityType,
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onAddReading(it)
                showAddDialog = false
            }
        )
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
    submeterId: Long?,
    utilityType: UtilityType?,
    onDismiss: () -> Unit,
    onConfirm: (Reading) -> Unit
) {
    var value by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    Reading(
                        submeterId = submeterId,
                        utilityType = utilityType,
                        reading = value.toDoubleOrNull(),
                        currentUnitPriceNet = price.toDoubleOrNull(),
                        date = runCatching { LocalDate.parse(date) }.getOrNull()
                    )
                )
            }) {
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
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Odczyt") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena netto") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data (YYYY-MM-DD)") },
                    singleLine = true
                )
            }
        }
    )
}