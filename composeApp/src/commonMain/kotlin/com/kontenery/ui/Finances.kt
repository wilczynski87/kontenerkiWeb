package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.library.utils.Month
import com.kontenery.model.ClientOnList
import com.kontenery.model.MonthValue
import com.kontenery.model.Payment
import com.kontenery.model.PaymentForFinanceTable
import com.kontenery.model.TableRowFinance
import com.kontenery.model.enums.ClientFilter
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate

@Composable
fun Finances(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val clients: List<ClientOnList> = state.clients
    val financeYear: Int = state.financeYear ?: LocalDate.now().year
    val financeTable: List<TableRowFinance> = state.financeTable

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

    when(windowSize) {
        WindowWidthSizeClass.Compact -> {
            Text("Finance - Compact")
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sortedClients) { client ->

                }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            Text("Finance - Expanded")
            val months: List<MonthValue> = Month.entries.map {
                MonthValue(
                    it.number.toString() + "-" + financeYear,
                    it.polishName
                )
            }

            PaymentsTable(
                viewModel,
                modifier = modifier,
                months = months,
                rows = financeTable
            )
        }
        WindowWidthSizeClass.Medium -> {
            Text("Finance - Medium")
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sortedClients) { client ->
                }
            }
        }

    }
}