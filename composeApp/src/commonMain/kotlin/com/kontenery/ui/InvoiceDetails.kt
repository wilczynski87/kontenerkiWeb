package com.kontenery.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.enums.Month
import com.kontenery.model.ModalData
import com.kontenery.model.enums.SellerAccount
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.model.enums.endOfCurrentMonth
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentMonth
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

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
    
    val columnsMap = mapOf(
        "nr_faktury" to ColumnConfig("Nr faktury", 170.dp) { it.invoiceNumber ?: "" },
        "data" to ColumnConfig("Data", 170.dp) { it.invoiceDate?.toString() ?: "" },
        "wyslano" to ColumnConfig("Wysłano", 200.dp) { it.invoiceSendToClient?.toString() ?: "" },
        "netto" to ColumnConfig("Netto", 140.dp) { it.priceSum ?: "" },
        "vat" to ColumnConfig("VAT", 120.dp) { it.vatAmountSum ?: "" },
        "brutto" to ColumnConfig("Brutto", 140.dp) { it.priceWithVatSum ?: "" },
        "platnosc" to ColumnConfig("Płatność", 210.dp) { it.paymentDay?.toString() ?: "" },
        "konto" to ColumnConfig("Konto", 190.dp) {
            SellerAccount.fromAccountNumber("PL${it.mainAccount}")?.name ?: ""
        },
        "klient" to ColumnConfig("Klient", 170.dp) { it.customer?.name ?: "" },
        "sprzedawca" to ColumnConfig("Sprzedawca", 210.dp) { it.seller?.name ?: "" },
        "typ" to ColumnConfig("Typ", 160.dp) { it.type ?: "" },
        "tytul" to ColumnConfig("Tytuł", 210.dp) { it.invoiceTitle ?: "" },
        "vatowiec" to ColumnConfig("Vatowiec", 210.dp) { if (it.vatApply) "Tak" else "Nie" },
        "akcja" to ColumnConfig("Akcja", 160.dp) { "" } // Special case for action button
    )

//    val tableWidth = widths.reduce { acc, dp -> acc + dp }
    val tableWidth = columnsMap.values.map { it.width }.reduce { acc, config -> acc + config }


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
                .heightIn(max = 600.dp)
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(tableWidth)
            ) {

                // HEADER
                Row {
                    Header(columnsMap["nr_faktury"])
                    Header(columnsMap["data"])
                    Header(columnsMap["wyslano"])
                    Header(columnsMap["netto"])
                    Header(columnsMap["vat"])
                    Header(columnsMap["brutto"])
                    Header(columnsMap["platnosc"])
                    Header(columnsMap["klient"])
                    Header(columnsMap["typ"])
                    Header(columnsMap["akcja"])
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                // BODY
                Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
                    invoices.forEach { invoice ->

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Cell(invoice.invoiceNumber ?: "", columnsMap["nr_faktury"])
                            Cell(invoice.invoiceDate?.toString() ?: "", columnsMap["data"])
                            Cell(invoice.invoiceSendToClient?.toString() ?: "", columnsMap["wyslano"])
                            Cell(invoice.priceSum ?: "", columnsMap["netto"])
                            Cell(invoice.vatAmountSum ?: "", columnsMap["vat"])
                            Cell(invoice.priceWithVatSum ?: "", columnsMap["brutto"])
                            Cell(invoice.paymentDay?.toString() ?: "", columnsMap["platnosc"])
                            Cell(invoice.customer?.name ?: "", columnsMap["klient"])
                            Cell(invoice.type ?: "", columnsMap["akcja"])

                            Box(
                                modifier = Modifier.width(columnsMap["akcja"]?.width ?: 150.dp),
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
fun Header(col: ColumnConfig? = null) {
    if(col == null) return
    Text(
        col.title,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(col.width)
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

@Composable
fun Cell(text: String, col: ColumnConfig? = null) {
    val width: Dp = col?.width ?: 150.dp
    Text(
        text,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(width)
            .padding(4.dp)
    )
}

data class ColumnConfig(
    val title: String,
    val width: Dp,
    val getValue: (Invoice) -> String
)