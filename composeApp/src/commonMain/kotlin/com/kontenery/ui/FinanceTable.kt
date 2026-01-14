package com.kontenery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kontenery.model.MonthValue
import com.kontenery.model.PaymentForFinanceTable
import com.kontenery.model.TableRowFinance
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.service.getMonthFinanceFromString
import com.kontenery.service.to2Decimals
import com.kontenery.service.unifyMonth

@Composable
fun TableCell(
    text: String,
    width: Dp = 120.dp,
    bold: Boolean = false,
    align: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(8.dp),
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = align
    )
}

//Komórka tabeli
@Composable
fun TableCell(
    payment: PaymentForFinanceTable?,
    width: Dp = 120.dp,
    viewModel: ParkingAppViewModel,
) {
    Column(
        modifier = Modifier
            .width(width)
            .padding(4.dp)
            .clickable {
            payment?.let { p ->
                viewModel.showConfirmModal(
                    dialogTitle = "Potwierdzenie",
                    dialogText = "Czy chcesz zatwierdzić płatność ${p.amount} z dnia ${p.date}?",
                    onConfirmation = {
                        // co się dzieje po potwierdzeniu
                        println("Potwierdzono płatność: ${p.amount}")
                    }
                )
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = payment?.date ?: "",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = payment?.amount?.to2Decimals() ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// Nagłówek tabeli (miesiące)
@Composable
fun TableHeader(months: List<MonthValue>) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TableCell("Nazwa", width = 220.dp, bold = true)

        months.forEach {
            Column {
                TableCell(it.month, bold = true, align = TextAlign.Center)
                TableCell(it.label, align = TextAlign.Center)
            }
        }
    }
}

// Wiersz tabeli
@Composable
fun TableDataRow(
    row: TableRowFinance,
    months: List<MonthValue>,
    viewModel: ParkingAppViewModel,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TableCell(row.name, width = 220.dp)

        months.forEach { date ->
            val payments = row.values[unifyMonth(date.month)]
            Column (
                modifier = Modifier
            ) {
                payments?.forEach { payment ->
                    TableCell(payment = payment, viewModel = viewModel)
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

// Cała tabela
@Composable
fun PaymentsTable(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier,
    months: List<MonthValue>,
    rows: List<TableRowFinance>
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader {
                TableHeader(months)
                HorizontalDivider()
            }

            items(rows) { row ->
                TableDataRow(row, months, viewModel)
                HorizontalDivider()
            }
        }
    }
}
