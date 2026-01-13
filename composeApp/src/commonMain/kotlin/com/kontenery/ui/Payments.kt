package com.example.parkingandroidview.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.model.Payment
import com.kontenery.library.model.invoice.Invoice
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import com.kontenery.model.Client
import com.kontenery.model.enums.CurrentScreen
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentYear
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.service.formatLocalDate
import com.kontenery.service.to2Decimals
import com.kontenery.ui.InvoicesTable
import com.kontenery.ui.PaymentsDetails

@Composable
fun PaymentsMenu(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val client: Client? = viewModel.state.collectAsState().value.client
    val invoices: List<Invoice> = viewModel.state.collectAsState().value.invoices
    val payments: List<Payment> = viewModel.state.collectAsState().value.payments

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Płatności dla:",
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .wrapContentSize(align = Alignment.Center)
                , textAlign = TextAlign.Center
                , style = MaterialTheme.typography.headlineMedium
            )
            ClientLinkName(viewModel, client)
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            PaymentsTable(viewModel, invoices, payments)
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            , verticalAlignment = CenterVertically
            , horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    viewModel.newPaymentState(clientId = client?.id)
                    viewModel.toPaymentForm(false)
                    viewModel.setGoBack(CurrentScreen.PAYMENT_MENU, CurrentScreen.PAYMENT_FORM)
                }
                , modifier = Modifier
            ) { Text("Dodaj płatność") }
        }
        PaymentsDetails(viewModel)
        InvoicesTable(viewModel)
    }
}

@Composable
fun PaymentsTable(viewModel : ParkingAppViewModel, invoices: List<Invoice>, payments: List<Payment>){
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
                    viewModel.updateForDate(LocalDate(year = from.year, monthNumber = monthNum, dayOfMonth = 1))
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
        months.add(current.monthNumber)
        current = current.plus(1, DateTimeUnit.MONTH)
    }
    months.reverse()

    return months
}

fun invoicesInMonth(month: Int, invoices: List<Invoice>): List<Invoice> {
    return invoices.filter { it.invoiceDate?.monthNumber == month }
}
fun paymentsInMonth(month: Int, payments: List<Payment>): List<Payment> {
    return payments.filter { it.date.monthNumber == month }
}

//@Preview(showBackground = true, widthDp = 500 )
//@Composable
//fun PaymentsMenuPreview() {
//    val viewModel = ParkingAppViewModel()
//    val client: Client = Client(clientPrivate = ClientPersonalData(firstName = "Test", lastName = "Test", pesel = "12345678901"), clientCompany = null, isActive = true)
//    val inv1 = Invoice(
//        invoiceNumber = "test num 1",
//        invoiceTitle = "faktura VAT",
//        invoiceDate = LocalDate.parse("2025-01-01"),
//        seller = Subject.Seller.company(null),
//        customer = Subject.Customer("test1", Address(), "test1", "test1"),
//        products = listOf(),
//        vatAmountSum = "230",
//        priceSum = "1000",
//        priceWithVatSum = "1230",
//        paymentDay = LocalDate.parse("2025-01-15"),
//        invoiceSendToClient = LocalDate.parse("2025-01-01"),
//        vatApply = false
//    )
//    val inv2 = Invoice(
//        invoiceNumber = "test num 2",
//        invoiceTitle = "faktura VAT",
//        invoiceDate = LocalDate.now(),
//        seller = Subject.Seller.company(null),
//        customer = Subject.Customer("test2", Address(), "test2", "test2"),
//        products = listOf(),
//        vatAmountSum = "115",
//        priceSum = "500",
//        priceWithVatSum = "615",
//        paymentDay = LocalDate.now(),
//        invoiceSendToClient = LocalDate.now(),
//        vatApply = true
//    )
//    val pay1 = Payment(
//        amount = BigDecimal(900),
//        date = LocalDate.parse("2025-01-07"),
//        fromClient = Client(),
//        method = "przelew",
//        toAccount = SellerAccount.BUSSINESS,
//        fromAccount = "0000 0000",
//        title = "test num 1",
//        forInvoices = listOf()
//    )
//    val pay2 = Payment(
//        amount = BigDecimal(200),
//        date = LocalDate.parse("2025-01-15"),
//        fromClient = Client(),
//        method = "przelew",
//        toAccount = SellerAccount.BUSSINESS,
//        fromAccount = "0000 0000",
//        title = "test num 1",
//        forInvoices = listOf()
//    )
//    val pay3 = Payment(
//        amount = BigDecimal(615),
//        date = LocalDate.now(),
//        fromClient = Client(),
//        method = "przelew",
//        toAccount = SellerAccount.BUSSINESS,
//        fromAccount = "0000 0000",
//        title = "test num 2",
//        forInvoices = listOf()
//    )
//    val invoices: List<Invoice> = listOf(inv1, inv2)
//    val payments: List<Payment> = listOf(pay1, pay2, pay3)
//    viewModel.updateClient(client)
//    viewModel.updateInvoicesAndPayments(invoices, payments)
//
//    PaymentsMenu(viewModel)
//}