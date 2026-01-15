package com.kontenery.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kontenery.model.invoice.Invoice
import com.kontenery.library.utils.Month
import kotlinx.datetime.LocalDate
import com.kontenery.model.ModalData
import com.kontenery.model.enums.SellerAccount
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.endOfCurrentMonth
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentMonth
import com.kontenery.service.ParkingAppViewModel

@Composable
fun InvoicesTable(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val forDate: LocalDate = state.forDate ?: LocalDate.now()
    val invoices: List<Invoice> = state.invoices
        .filter {
            it.invoiceDate!! >= LocalDate.startOfCurrentMonth(forDate) &&
            it.invoiceDate <= LocalDate.endOfCurrentMonth(forDate)
        }

    val columnWidths = remember {
        mutableStateListOf(
            200, // invoiceNumberWidth
            200, // dateWidth
            200, // sendDateWidth
            140, // priceWidth
            120,  // vatWidth
            140, // totalWidth
            210, // paymentDateWidth
            190, // mainAccountWidth
            170, // customerWidth
            210, // sellerWidth
            160, // typeWidth
            150, // titleWidth
            100,  // vatApplyWidth
            160   // actionWidth
        )
    }

    val tableWidth by remember {
        derivedStateOf { columnWidths.sum() }
    }

    val scrollState = rememberScrollState()

//        println(columnWidths)
//        println(tableWidth)
    Column(
        modifier = modifier
            .padding(8.dp)
            .horizontalScroll(scrollState)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
        ) {
            Text(
                text = "Faktury za ${Month.fromNumber(forDate.monthNumber)?.polishName}:",
                textAlign = TextAlign.Center,
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray,
            thickness = 0.5.dp
        )

        // Nagłówek
        Row {
            TableHeaderCell("Nr faktury", 0, columnWidths)
            TableHeaderCell("Data", 1, columnWidths)
            TableHeaderCell("Wysłano do klienta", 2, columnWidths)
            TableHeaderCell("Cena netto", 3, columnWidths)
            TableHeaderCell("VAT", 4, columnWidths)
            TableHeaderCell("Cena brutto", 5, columnWidths)
            TableHeaderCell("Dzień płatności", 6, columnWidths)
            TableHeaderCell("Konto", 7, columnWidths)
            TableHeaderCell("Klient", 8, columnWidths)
            TableHeaderCell("Sprzedawca", 9, columnWidths)
            TableHeaderCell("Typ", 10, columnWidths)
            TableHeaderCell("Tytuł", 11, columnWidths)
            TableHeaderCell("VAT", 12, columnWidths)
            TableHeaderCell("Wyślij ponownie", 13, columnWidths)
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray,
            thickness = 0.5.dp
        )

        // Body
        invoices.forEach { invoice ->
            val accountType =
                SellerAccount.fromAccountNumber("PL${invoice.mainAccount}")?.name ?: ""
            Row(
                modifier = Modifier, verticalAlignment = Alignment.CenterVertically
            ) {
                TableBodyCell(invoice.invoiceNumber ?: "", 0, columnWidths)
                TableBodyCell(invoice.invoiceDate?.toString() ?: "", 1, columnWidths)
                TableBodyCell(invoice.invoiceSendToClient?.toString() ?: "", 2, columnWidths)
                TableBodyCell(invoice.priceSum ?: "", 3, columnWidths)
                TableBodyCell(invoice.vatAmountSum ?: "", 4, columnWidths)
                TableBodyCell(invoice.priceWithVatSum ?: "", 5, columnWidths)
                TableBodyCell(invoice.paymentDay?.toString() ?: "", 6, columnWidths)
                TableBodyCell(accountType, 7, columnWidths)
                TableBodyCell(invoice.customer?.name ?: "", 8, columnWidths)
                TableBodyCell(invoice.seller?.name ?: "", 9, columnWidths)
                TableBodyCell(invoice.type ?: "", 10, columnWidths)
                TableBodyCell(invoice.invoiceTitle ?: "", 11, columnWidths)
                TableBodyCell(if (invoice.vatApply) "Tak" else "Nie", 12, columnWidths)
                TableBodyCell(13, columnWidths) {
                    if (invoice.invoiceNumber != null)
                        IconButton(onClick = {
                            viewModel.createConfirmationModal(
                                ModalData(
                                    onDismissRequest = { viewModel.closeConfirmationModal() },
                                    onConfirmation = {
                                        viewModel.postPeriodicInvoiceAgain(invoice.invoiceNumber)
                                        viewModel.closeConfirmationModal()
                                    },
                                    dialogTitle = "Wyślij ponownie",
                                    dialogText = "Czy wysłać fakturę nr: ${invoice.invoiceNumber} ponownie?",
                                    icon = Icons.Default.Info
                                )
                            )
                        }) {
                            Icon(Icons.Default.Email, contentDescription = "email again")
                        }
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color.LightGray,
                thickness = 0.5.dp
            )
        }
    }
//    }
}

@Composable
fun TableHeaderCell(text: String, index: Int, columnWidths: MutableList<Int>) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(4.dp)
            .sharedWidthModifier(index, columnWidths)
    )
}

@Composable
fun TableBodyCell(text: String, index: Int, columnWidths: MutableList<Int>) {
    Text(
        text,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(4.dp)
            .sharedWidthModifier(index, columnWidths)
    )
}

@Composable
fun TableBodyCell(index: Int, columnWidths: MutableList<Int>, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .sharedWidthModifier(index, columnWidths),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun Modifier.sharedWidthModifier(
    index: Int,
    columnWidths: MutableList<Int>
): Modifier {
    val density = LocalDensity.current
    if (columnWidths.size <= index) {
        repeat(index - columnWidths.size + 1) { columnWidths.add(0) }
    }

    val widthDp = with(density) { columnWidths[index].coerceAtLeast(80).toDp() } // minimalna szerokość

    return this
        .onGloballyPositioned { coordinates ->
            val width = coordinates.size.width
            if (width > columnWidths[index]) {
                columnWidths[index] = width
            }
        }
        .width(widthDp)
}



//@Preview(showBackground = true, widthDp = 360 )
//@Composable
//fun InvoiceDetailsPreview() {
//    val invoices = listOf(
//        Invoice(
//            invoiceNumber = "FV/2025/001",
//            invoiceTitle = "Faktura VAT za usługi IT",
//            invoiceDate = LocalDate.parse("2025-08-01"),
//            seller = Subject.Seller(name = "Firma Sprzedawca A"),
//            customer = Subject.Customer(
//                name = "Klient A",
//                address = Address(),
//                nip = "11111111111111",
//                email = "klient1@gmail.com",
//                phone = "5555555555",
//                client = Client()
//            ),
//            products = listOf(
//                Position(productName = "Produkt 1", quantity = "2", unitPrice = "100.00"),
//                Position(productName = "Produkt 2", quantity = "1", unitPrice = "200.00")
//            ),
//            vatAmountSum = "70.00",
//            priceSum = "300.00",
//            priceWithVatSum = "370.00",
//            paymentDay = LocalDate.parse("2025-8-15"),
//            invoiceSendToClient = LocalDate.parse("2025-8-02"),
//            type = InvoiceType.PERIODIC.name,
//            vatApply = true
//        ),
//        Invoice(
//            invoiceNumber = "FV/2025/002",
//            invoiceTitle = "Faktura VAT za sprzęt",
//            invoiceDate = LocalDate.parse("2025-8-03"),
//            seller = Subject.Seller(name = "Firma Sprzedawca B"),
//            customer = Subject.Customer(
//                name = "Klient B",
//                address = Address(),
//                nip = "22222222222222222",
//                email = "klient2@gmail.com",
//                phone = "666666666666666",
//                client = Client()
//            ),
//            products = listOf(
//                Position(productName = "Produkt A", quantity = "5", unitPrice = "50.00")
//            ),
//            vatAmountSum = "25.00",
//            priceSum = "250.00",
//            priceWithVatSum = "275.00",
//            paymentDay = LocalDate.parse("2025-8-18"),
//            invoiceSendToClient = LocalDate.parse("2025-8-04"),
//            type = InvoiceType.PERIODIC.name,
//            vatApply = true
//        ),
//        Invoice(
//            invoiceNumber = "FV/2025/003",
//            invoiceTitle = "Faktura VAT za konsultacje",
//            invoiceDate = LocalDate.parse("2025-8-05"),
//            seller = Subject.Seller(name = "Firma Sprzedawca C"),
//            customer = Subject.Customer(
//                name = "Klient C",
//                address = Address(),
//                nip = "333333333333333",
//                email = "klient3@gmail.com",
//                phone = "33333333333333",
//                client = Client()
//            ),
//            products = listOf(
//                Position(productName = "Konsultacja 1h", quantity = "3", unitPrice = "150.00")
//            ),
//            vatAmountSum = "67.50",
//            priceSum = "450.00",
//            priceWithVatSum = "517.50",
//            paymentDay = LocalDate.parse("2025-8-20"),
//            invoiceSendToClient = LocalDate.parse("2025-8-06"),
//            type = InvoiceType.PERIODIC.name,
//            vatApply = true
//        )
//    )
//    val viewModel = ParkingAppViewModel()
//    viewModel.updateInvoicesAndPayments(invoices, listOf())
//
//    InvoicesTable(viewModel)
//}
