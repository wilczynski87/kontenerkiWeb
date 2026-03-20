package com.kontenery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.model.Payment
import com.kontenery.model.invoice.Invoice
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import com.kontenery.model.Client
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentYear
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.util.formatLocalDate
import com.kontenery.util.to2Decimals
import kotlinx.datetime.number

//@Composable
//fun PaymentsMenu(
//    viewModel: ParkingAppViewModel,
//    windowSize: WindowWidthSizeClass,
//    modifier: Modifier = Modifier
//) {
//    val state by viewModel.state.collectAsState()
//    val client: Client? = state.client
//    val invoices: List<Invoice> = state.invoices
//    val payments: List<Payment> = state.payments
//    val financeYear: Int = state.financeYear ?: LocalDate.now().year
//    val from = LocalDate.parse("${financeYear}-01-01")
//    val to = if(financeYear == LocalDate.now().year) LocalDate.now()
//        else LocalDate.parse("${financeYear}-12-31")
////    println("financeYear: $financeYear, from: $from, to: $to")
//
//    Column(
//        modifier = modifier
//            .padding(horizontal = 16.dp)
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//    ) {
//        FlowRow(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "Płatności dla:",
//                modifier = Modifier
//                    .padding(horizontal = 4.dp, vertical = 4.dp)
//                    .wrapContentSize(align = Alignment.Center)
//                , textAlign = TextAlign.Center
//                , style = MaterialTheme.typography.headlineMedium
//            )
//            ClientLinkName(viewModel, client)
//        }
//
//        Row(modifier = Modifier.fillMaxWidth()) {
//            Column {
//                YearPager(financeYear, onYearChange = {
////                    println("Zmiana roku: $it")
//                    viewModel.changeFinanceYearPaymentsMenu(it)
//                })
//                PaymentsTable(viewModel, invoices, payments, from, to)
//            }
//        }
//
//        Row(modifier = Modifier
//            .fillMaxWidth()
//            , verticalAlignment = CenterVertically
//            , horizontalArrangement = Arrangement.End
//        ) {
//            Button(
//                onClick = {
//                    viewModel.newPaymentState(clientId = client?.id)
//                    viewModel.toPaymentForm(false)
//                    viewModel.setGoBack(CurrentScreen.PAYMENT_MENU, CurrentScreen.PAYMENT_FORM)
//                }
//                , modifier = Modifier
//            ) { Text("Dodaj płatność") }
//        }
//        PaymentsDetails(viewModel)
//        Box(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            InvoicesTable(viewModel, windowSize)
//        }
//    }
//}
@Composable
fun PaymentsMenu(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    val client: Client? = state.client
    val invoices: List<Invoice> = state.invoices
    val payments: List<Payment> = state.payments

    val financeYear: Int = state.financeYear ?: LocalDate.now().year
    val from = LocalDate.parse("${financeYear}-01-01")
    val to = if (financeYear == LocalDate.now().year) LocalDate.now()
    else LocalDate.parse("${financeYear}-12-31")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        // 🔹 HEADER
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Płatności dla:",
                    modifier = Modifier.padding(4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
                ClientLinkName(viewModel, client)
            }
        }

        // 🔹 YEAR + PAYMENTS TABLE
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column {
                    YearPager(
                        financeYear,
                        onYearChange = {
                            viewModel.changeFinanceYearPaymentsMenu(it)
                        }
                    )
                    PaymentsTable(viewModel, invoices, payments, from, to)
                }
            }
        }

        // 🔹 BUTTON
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        viewModel.newPaymentState(clientId = client?.id)
                        viewModel.toPaymentForm(false)
                        viewModel.setGoBack(
                            CurrentScreen.PAYMENT_MENU,
                            CurrentScreen.PAYMENT_FORM
                        )
                    }
                ) {
                    Text("Dodaj płatność")
                }
            }
        }

        // 🔹 DETAILS
        item {
            PaymentsDetails(viewModel)
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth() // daje poprawne constrainty
            ) {
                InvoicesTable(viewModel, windowSize)
            }
        }
    }
}

@Composable
fun PaymentsTable(
    viewModel : ParkingAppViewModel,
    invoices: List<Invoice>,
    payments: List<Payment>,
    from: LocalDate = LocalDate.startOfCurrentYear(),
    to: LocalDate = LocalDate.now()
){
    val columnWeight = listOf(1f, 1f, 1f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {

        PaymentsTableHeader(weights = columnWeight)
        PaymentsTableFooter(
            weights = columnWeight,
            invoices = invoices,
            payments = payments,
        )
        PaymentsTableRows(
            viewModel = viewModel,
            weights = columnWeight,
            invoices = invoices,
            payments = payments,
            from = from,
            to = to,
        )
    }
}

@Composable
fun PaymentsTableHeader(
    weights: List<Float>,
){
    val values = listOf("Data", "Faktura:", "Płatność:", "Suma:")

    Row(modifier = Modifier.fillMaxWidth()) {
        values.forEachIndexed { index, value ->
            Text(
                text = value,
                modifier = Modifier
                    .weight(weights.getOrElse(index) { 1f })
                    .padding(4.dp)
                , style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun PaymentsTableRows(
    viewModel: ParkingAppViewModel,
    weights: List<Float>,
    invoices: List<Invoice>,
    payments: List<Payment>,
    from: LocalDate = LocalDate.startOfCurrentYear(),
    to: LocalDate = LocalDate.now()
){
    val months = listOf("Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień")

    val counter: List<Int> = monthsBetween(from, to)

    Column(modifier = Modifier.fillMaxWidth()) {
        counter.forEach { monthNum ->
            val monthInvoices = invoicesInMonth(monthNum, invoices)
            val monthPayments = paymentsInMonth(monthNum, payments)
            val sumRow: Double = calculateSum(monthInvoices, monthPayments)
            val rowColor: Color = if(sumRow < 0) Color.hsl(360f, 1f, 0.6f, 0.85f,)
                else if(sumRow > 0) Color.hsl(120f, 1f, 0.6f, 0.85f)
                else Color.Black

            Row(modifier = Modifier
                .padding(1.dp)
                .clickable {
                    viewModel.updateForDate(LocalDate(year = from.year, month = monthNum, day = 1))
                }
            ) {
                // data:
                Text(
                    text = "${months[monthNum - 1]} $monthNum",
                    modifier = Modifier
                        .weight(weights[0])
                        .padding(4.dp), style = MaterialTheme.typography.bodyMedium
                )
                // faktura:
                if(monthInvoices.isNotEmpty()) {
                    ListInvoices(monthInvoices, Modifier.weight(weights[1]).padding(1.dp))
                } else {
                    Text(
                        text = "-",
                        modifier = Modifier
                            .weight(weights[1])
                            .padding(4.dp), style = MaterialTheme.typography.bodyMedium)
                }
                // płatność:
                if(monthPayments.isNotEmpty()) {
                    ListPayments(monthPayments, Modifier.weight(weights[2]).padding(1.dp))
                } else {
                    Text(
                        text = "-",
                        modifier = Modifier
                            .weight(weights[2])
                            .padding(4.dp), style = MaterialTheme.typography.bodyMedium)
                }
                // suma:
                if(monthPayments.isNotEmpty() || monthInvoices.isNotEmpty()) {
                    Text(
                        // TODO: poprawić kalkulacje
                        text = sumRow.to2Decimals(),
                        modifier = Modifier
                            .weight(weights[3])
                            .padding(4.dp), style = MaterialTheme.typography.bodyMedium
                            .copy(color = rowColor)

                    )
                } else {
                    Text(
                        text = "-",
                        modifier = Modifier
                            .weight(weights[3])
                            .padding(4.dp), style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp)
        }
    }
}

fun calculateSum(invoices: List<Invoice>, payments: List<Payment>): Double {
    val invoicesSum = invoices.sumOf { (it.priceWithVatSum ?: it.priceSum)?.toDoubleOrNull() ?: 0.0 }
    val paymentsSum = payments.sumOf { it.amount }
    return (paymentsSum - invoicesSum)
}

@Composable
fun ListInvoices(
    invoices: List<Invoice>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        for(invoice in invoices) {
            Column(modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = invoice.invoiceNumber.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = (invoice.priceWithVatSum ?: invoice.priceSum).toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
@Composable
fun ListPayments(
    payments: List<Payment>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        for(payment in payments) {
            val date = payment.date
            val formattedDate = formatLocalDate(date)
            Column(modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = payment.amount.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun PaymentsTableFooter(
    weights: List<Float>,
    invoices: List<Invoice>,
    payments: List<Payment>,
){
    val sumInvoices = invoices.sumOf { (it.priceWithVatSum ?: it.priceSum)?.toDoubleOrNull() ?: 0.0 }
    val sumPayments = payments.sumOf { it.amount }
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Suma",
            modifier = Modifier
                .weight(weights[0])
                .padding(4.dp), style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = sumInvoices.to2Decimals(),
            modifier = Modifier
                .weight(weights[1])
                .padding(4.dp), style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = sumPayments.to2Decimals(),
            modifier = Modifier
                .weight(weights[2])
                .padding(4.dp), style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = (sumPayments - sumInvoices).to2Decimals(),
            modifier = Modifier
                .weight(weights[3])
                .padding(4.dp), style = MaterialTheme.typography.bodyMedium
            , color = if((sumPayments - sumInvoices) < 0) MaterialTheme.colorScheme.error else Color.Green
        )
    }
    HorizontalDivider()
}

@Composable
fun ClientLinkName(
    viewModel: ParkingAppViewModel,
    client: Client?,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = { viewModel.toClientData(client?.id) }
        , modifier = modifier
            .padding(end = 4.dp)
            .wrapContentSize()
        , contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        , shape = RoundedCornerShape(25)
    ) {
        Text("${client?.getName()}",
            modifier = modifier
                .wrapContentSize(align = Alignment.Center)
            , textAlign = TextAlign.Center
            , style = MaterialTheme.typography.headlineMedium
        )
    }
}

fun monthsBetween(from: LocalDate = LocalDate.startOfCurrentYear(), to: LocalDate = LocalDate.now()): List<Int> {
    require(from <= to ) { "Start date must be before or equal to end date" }

    val months = mutableListOf<Int>()
    var current = from

    while (current <= to) {
        months.add(current.month.number)
        current = current.plus(1, DateTimeUnit.MONTH)
    }
    months.reverse()

    return months
}

fun invoicesInMonth(month: Int, invoices: List<Invoice>): List<Invoice> {
    return invoices.filter { it.invoiceDate?.month?.number == month }
}
fun paymentsInMonth(month: Int, payments: List<Payment>): List<Payment> {
    return payments.filter { it.date.month.number == month }
}
