package com.kontenery.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kontenery.model.invoice.Invoice
import com.kontenery.library.utils.Month
import com.kontenery.model.ModalData
import com.kontenery.model.enums.SellerAccount
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.endOfCurrentMonth
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentMonth
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

//@Composable
//fun InvoicesTable(
//    viewModel: ParkingAppViewModel,
//    windowSize: WindowWidthSizeClass,
//    modifier: Modifier = Modifier
//) {
//    val state by viewModel.state.collectAsState()
//    val forDate: LocalDate = state.forDate ?: LocalDate.now()
//    val invoices: List<Invoice> = state.invoices
//        .filter {
//            it.invoiceDate!! >= LocalDate.startOfCurrentMonth(forDate) &&
//            it.invoiceDate <= LocalDate.endOfCurrentMonth(forDate)
//        }
//
//    val columnWidths = remember {
//        mutableStateListOf(
//            200, // invoiceNumberWidth
//            200, // dateWidth
//            200, // sendDateWidth
//            140, // priceWidth
//            120,  // vatWidth
//            140, // totalWidth
//            210, // paymentDateWidth
//            190, // mainAccountWidth
//            170, // customerWidth
//            210, // sellerWidth
//            160, // typeWidth
//            150, // titleWidth
//            100,  // vatApplyWidth
//            160   // actionWidth
//        )
//    }
//
//    val tableWidth by remember {
//        derivedStateOf { columnWidths.sum() }
//    }
//    val modifierWidth = Modifier.width(with(LocalDensity.current) { tableWidth.toDp() })
//
//    val scrollState = rememberScrollState()
//
////        println(columnWidths)
////        println(tableWidth)
//    Column(
//        modifier = modifier
//            .padding(8.dp)
////            .horizontalScroll(scrollState)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clipToBounds()
//                .horizontalScroll(scrollState)
//        ) {
//            Column(
//                modifier = Modifier.width(with(LocalDensity.current) { tableWidth.toDp() })
//            ) {
//
//
//            Row(
//                modifier = modifierWidth
////            .fillMaxWidth(),
//            ) {
//                Text(
//                    text = "Faktury za ${Month.fromNumber(forDate.month.number)?.polishName}:",
//                    textAlign = TextAlign.Center,
//                )
//            }
//            HorizontalDivider(
//                modifier = modifierWidth,
////                .fillMaxWidth(),
//                color = Color.LightGray,
//                thickness = 0.5.dp
//            )
//
//            // Nagłówek
//            Row(
//                modifier = modifierWidth,
//            ) {
//                TableHeaderCell("Nr faktury", 0, columnWidths)
//                TableHeaderCell("Data", 1, columnWidths)
//                TableHeaderCell("Wysłano do klienta", 2, columnWidths)
//                TableHeaderCell("Cena netto", 3, columnWidths)
//                TableHeaderCell("VAT", 4, columnWidths)
//                TableHeaderCell("Cena brutto", 5, columnWidths)
//                TableHeaderCell("Dzień płatności", 6, columnWidths)
//                TableHeaderCell("Konto", 7, columnWidths)
//                TableHeaderCell("Klient", 8, columnWidths)
//                TableHeaderCell("Sprzedawca", 9, columnWidths)
//                TableHeaderCell("Typ", 10, columnWidths)
//                TableHeaderCell("Tytuł", 11, columnWidths)
//                TableHeaderCell("VAT", 12, columnWidths)
//                TableHeaderCell("Wyślij ponownie", 13, columnWidths)
//            }
//            HorizontalDivider(
//                modifier = modifierWidth,
//                color = Color.LightGray,
//                thickness = 0.5.dp
//            )
//
//            // Body
//            invoices.forEach { invoice ->
//                val accountType =
//                    SellerAccount.fromAccountNumber("PL${invoice.mainAccount}")?.name ?: ""
//                Row(
//                    modifier = modifierWidth, verticalAlignment = Alignment.CenterVertically
//                ) {
//                    TableBodyCell(invoice.invoiceNumber ?: "", 0, columnWidths)
//                    TableBodyCell(invoice.invoiceDate?.toString() ?: "", 1, columnWidths)
//                    TableBodyCell(invoice.invoiceSendToClient?.toString() ?: "", 2, columnWidths)
//                    TableBodyCell(invoice.priceSum ?: "", 3, columnWidths)
//                    TableBodyCell(invoice.vatAmountSum ?: "", 4, columnWidths)
//                    TableBodyCell(invoice.priceWithVatSum ?: "", 5, columnWidths)
//                    TableBodyCell(invoice.paymentDay?.toString() ?: "", 6, columnWidths)
//                    TableBodyCell(accountType, 7, columnWidths)
//                    TableBodyCell(invoice.customer?.name ?: "", 8, columnWidths)
//                    TableBodyCell(invoice.seller?.name ?: "", 9, columnWidths)
//                    TableBodyCell(invoice.type ?: "", 10, columnWidths)
//                    TableBodyCell(invoice.invoiceTitle ?: "", 11, columnWidths)
//                    TableBodyCell(if (invoice.vatApply) "Tak" else "Nie", 12, columnWidths)
//                    TableBodyCell(13, columnWidths) {
//                        if (invoice.invoiceNumber != null)
//                            IconButton(onClick = {
//                                viewModel.createConfirmationModal(
//                                    ModalData(
//                                        onDismissRequest = { viewModel.closeConfirmationModal() },
//                                        onConfirmation = {
//                                            viewModel.postPeriodicInvoiceAgain(invoice.invoiceNumber)
//                                            viewModel.closeConfirmationModal()
//                                        },
//                                        dialogTitle = "Wyślij ponownie",
//                                        dialogText = "Czy wysłać fakturę nr: ${invoice.invoiceNumber} ponownie?",
//                                        icon = Icons.Default.Info
//                                    )
//                                )
//                            }) {
//                                Icon(Icons.Default.Email, contentDescription = "email again")
//                            }
//                    }
//                }
//                HorizontalDivider(
//                    modifier = Modifier
//                        .width(with(LocalDensity.current) { tableWidth.toDp() }),
//                    color = Color.LightGray,
//                    thickness = 0.5.dp
//                )
//            }
//        }}
//    }
////    }
//}

//@Composable
//fun TableHeaderCell(text: String, index: Int, columnWidths: MutableList<Int>) {
//    Text(
//        text,
//        fontWeight = FontWeight.Bold,
//        textAlign = TextAlign.Center,
//        modifier = Modifier
//            .padding(4.dp)
//            .sharedWidthModifier(index, columnWidths)
//    )
//}
//
//@Composable
//fun TableBodyCell(text: String, index: Int, columnWidths: MutableList<Int>) {
//    Text(
//        text,
//        textAlign = TextAlign.Center,
//        modifier = Modifier
//            .padding(4.dp)
//            .sharedWidthModifier(index, columnWidths)
//    )
//}
//
//@Composable
//fun TableBodyCell(index: Int, columnWidths: MutableList<Int>, content: @Composable () -> Unit) {
//    Box(
//        modifier = Modifier
//            .padding(4.dp)
//            .sharedWidthModifier(index, columnWidths),
//        contentAlignment = Alignment.Center
//    ) {
//        content()
//    }
//}

@Composable
fun InvoicesTable(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val forDate = state.forDate ?: LocalDate.now()
    val invoices = state.invoices.filter {
        it.invoiceDate!! >= LocalDate.startOfCurrentMonth(forDate) &&
                it.invoiceDate <= LocalDate.endOfCurrentMonth(forDate)
    }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val widths = listOf(
        200.dp, 200.dp, 200.dp, 140.dp, 120.dp, 140.dp,
        210.dp, 190.dp, 170.dp, 210.dp, 160.dp, 150.dp,
        100.dp, 160.dp
    )
    val tableWidth = widths.reduce { acc, dp -> acc + dp }

    Column(modifier = modifier.padding(8.dp)) {

        Text(
            text = "Faktury za ${Month.fromNumber(forDate.month.number)?.polishName}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 🔹 HORIZONTAL + VERTICAL SCROLL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // 🔹 tutaj ustalamy max wysokość scrolla
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(modifier = Modifier.width(tableWidth)) {

                // HEADER
                Row {
                    widths.forEachIndexed { index, width ->
                        Header(
                            text = when (index) {
                                0 -> "Nr faktury"
                                1 -> "Data"
                                2 -> "Wysłano"
                                3 -> "Netto"
                                4 -> "VAT"
                                5 -> "Brutto"
                                6 -> "Płatność"
                                7 -> "Konto"
                                8 -> "Klient"
                                9 -> "Sprzedawca"
                                10 -> "Typ"
                                11 -> "Tytuł"
                                12 -> "VAT"
                                13 -> "Akcja"
                                else -> ""
                            },
                            width = width
                        )
                    }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                // BODY
                Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
                    invoices.forEach { invoice ->
                        val accountType =
                            SellerAccount.fromAccountNumber("PL${invoice.mainAccount}")?.name ?: ""

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Cell(invoice.invoiceNumber ?: "", widths[0])
                            Cell(invoice.invoiceDate?.toString() ?: "", widths[1])
                            Cell(invoice.invoiceSendToClient?.toString() ?: "", widths[2])
                            Cell(invoice.priceSum ?: "", widths[3])
                            Cell(invoice.vatAmountSum ?: "", widths[4])
                            Cell(invoice.priceWithVatSum ?: "", widths[5])
                            Cell(invoice.paymentDay?.toString() ?: "", widths[6])
                            Cell(accountType, widths[7])
                            Cell(invoice.customer?.name ?: "", widths[8])
                            Cell(invoice.seller?.name ?: "", widths[9])
                            Cell(invoice.type ?: "", widths[10])
                            Cell(invoice.invoiceTitle ?: "", widths[11])
                            Cell(if (invoice.vatApply) "Tak" else "Nie", widths[12])

                            Box(
                                modifier = Modifier.width(widths[13]),
                                contentAlignment = Alignment.Center
                            ) {
                                if (invoice.invoiceNumber != null) {
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
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun Header(text: String, width: Dp) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(width)
            .padding(4.dp)
    )
}

@Composable
fun Cell(text: String, width: Dp) {
    Text(
        text,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(width)
            .padding(4.dp)
    )
}
