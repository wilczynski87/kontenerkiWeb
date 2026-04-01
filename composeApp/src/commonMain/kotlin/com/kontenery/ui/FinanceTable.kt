package com.kontenery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kontenery.library.utils.Month
import com.kontenery.model.MonthValue
import com.kontenery.model.PaymentForFinanceTable
import com.kontenery.model.TableRowFinance
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.util.to2Decimals
import com.kontenery.util.unifyMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

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
//                payment?.let { p ->
//                    viewModel.showConfirmModal(
//                        dialogTitle = "Potwierdzenie",
//                        dialogText = "Czy chcesz Przejść do płatność?",
//                        onConfirmation = {
//                            // co się dzieje po potwierdzeniu
//                            println("Potwierdzono płatność: ${p.amount}")
//                        }
//                    )
//                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(payment?.amount == 0.00) {
//            Text(Month.fromString(payment.date ?: "")?.polishName ?: "brak daty" )
            Spacer(modifier = Modifier.height(1.dp))
//            Text("BRAK")
        } else {
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
}

@Composable
fun TableCell(
    prevYearsBalance: Double?,
    width: Dp = 120.dp,
    viewModel: ParkingAppViewModel,
) {
    val state by viewModel.state.collectAsState()
    val financeForYear: Int = state.financeYear ?: LocalDate.now().year

    Column(
        modifier = Modifier
            .width(width)
            .padding(4.dp)
            .clickable {
//                payment?.let { p ->
//                    viewModel.showConfirmModal(
//                        dialogTitle = "Potwierdzenie",
//                        dialogText = "Czy chcesz zatwierdzić płatność ${p.amount} z dnia ${p.date}?",
//                        onConfirmation = {
//                            // co się dzieje po potwierdzeniu
//                            println("Potwierdzono płatność: ${p.amount}")
//                        }
//                    )
//                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = lastYears(LocalDate.parse("${financeForYear - 5}-01-01"), LocalDate.parse("${financeForYear}-12-31")),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = prevYearsBalance?.to2Decimals() ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun lastYears(from: LocalDate = LocalDate.now().minus(5, DateTimeUnit.YEAR), to: LocalDate = LocalDate.now()): String {
    return "${from.year}/${to.year}"
}

@Composable
fun TableCellButton(
    width: Dp = 220.dp,
    bold: Boolean = false,
    align: Alignment = Alignment.Center,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .width(width)
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = align
    ) {
        ProvideTextStyle(
            value = LocalTextStyle.current.copy(
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
            )
        ) {
            content()
        }
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

        TableCell("Poprzednie \nlata:", bold = true)


        months.forEach {
            Column {
                TableCell(it.month, bold = true, align = TextAlign.Center)
                TableCell(it.label, align = TextAlign.Center)
            }
        }
    }
}

// Wiersz tabeli z płatnościami klientów
@Composable
fun TableDataRow(
    row: TableRowFinance,
    months: List<MonthValue>,
    viewModel: ParkingAppViewModel,
) {
    val alfaFinanceTableActive = if (row.isActive) 1f else 0.5f
    val colorFinanceTableActive = if (row.isActive) MaterialTheme.colorScheme.onPrimary else Color.LightGray
    // Decide if client name will be red or green
    val clientOverdue = when {
        row.clientOverdue == null -> Modifier
        row.clientOverdue > 0.00 -> {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Green, Color.Transparent)
                ),
                shape = MaterialTheme.shapes.small,
                alpha = 0.5f
            )
        }
        row.clientOverdue < 0.00 -> Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Red, Color.Transparent)
            ),
            shape = MaterialTheme.shapes.small,
            alpha = 0.5f
        )
        else -> Modifier
    }


        Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .alpha(alfaFinanceTableActive)
                .background(colorFinanceTableActive)
        ) {
            TableCellButton(onClick = {
                    if (row.clientId == null) return@TableCellButton
                    viewModel.fetchClient(row.clientId)
                    viewModel.fetchPaymentsForClient(row.clientId)
                    viewModel.fetchInvoicesForClient(row.clientId)
                    viewModel.toPaymentsMenu()
                },
                modifier = clientOverdue
            ) {
                Text(row.name, modifier = Modifier)
            }

            // Previews Years Balance:
            Column {
                TableCell(prevYearsBalance = row.prevYearsBalance, viewModel = viewModel)
            }

            // Current Payments
            months.forEachIndexed { index, date ->
                val payments = row.values[unifyMonth(date.month)]
                Column(
                    modifier = Modifier
                ) {
                    payments?.forEach { payment ->
                        TableCell(payment = payment, viewModel = viewModel)
                    }
                }
                if(payments.isNullOrEmpty().not()) VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth(), 1.dp, Color.Blue)
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
    val state by viewModel.state.collectAsState()
    val financeYear: Int = state.financeYear ?: LocalDate.now().year

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    YearPager(
                        year = financeYear,
                        onYearChange = { viewModel.onFinanceYearChange(it) }
                    )
                }
                TableHeader(months)
                HorizontalDivider()
            }

            items(rows) { row ->
                TableDataRow(row, months, viewModel)
            }
        }
    }
}

@Composable
fun YearPager(
    year: Int,
    onYearChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
    ) {
        IconButton(onClick = { onYearChange(year - 1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous year")
        }

        Text(
            text = year.toString(),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (year < LocalDate.now().year) IconButton(onClick = { onYearChange(year + 1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next year")
        }
    }
}