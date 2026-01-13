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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.kontenery.model.Payment
import com.kontenery.library.utils.Month
import com.kontenery.model.ModalData
import com.kontenery.model.enums.endOfCurrentMonth
import com.kontenery.model.enums.now
import com.kontenery.model.enums.startOfCurrentMonth
import com.kontenery.service.ParkingAppViewModel
import kotlinx.datetime.LocalDate

@Composable
fun PaymentsDetails(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val forDate: LocalDate = state.forDate ?: LocalDate.now()
    val payments: List<Payment> = state.payments
        .filter {
            it.date >= LocalDate.startOfCurrentMonth(forDate) &&
            it.date <= LocalDate.endOfCurrentMonth(forDate)
        }

    val scrollState = rememberScrollState()

    // tu przechowujemy zmierzone szerokości kolumn
    val columnWidths = remember { mutableStateListOf(
        200, // data
        150, // kwota
        150, // tytuł
        200, // metoda
        200, // za faktury
        150, // akcja
    ) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        HeaderPayments(forDate, columnWidths)
        BodyPayments(payments, viewModel, columnWidths)
    }
}

@Composable
fun HeaderPayments(date: LocalDate, columnWidths: MutableList<Int>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Płatności za ${Month.fromNumber(date.monthNumber)?.polishName}:"
            , textAlign = TextAlign.Center,
        )
    }
    HorizontalDivider(
        thickness = 1.dp
        , color = Color.Black
    )
    Row {
        HeaderCell("Data", 0, columnWidths)
        HeaderCell("Kwota", 1, columnWidths)
        HeaderCell("Tytułem", 2, columnWidths)
        HeaderCell("Metoda", 3, columnWidths)
        HeaderCell("Za faktury", 4, columnWidths)
        HeaderCell("Akcja", 5, columnWidths)
    }
}

@Composable
fun BodyPayments(payments: List<Payment>, viewModel: ParkingAppViewModel, columnWidths: MutableList<Int>) {
    Column {
        payments.forEach { payment ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                BodyCell(payment.date.toString(), 0, columnWidths)
                BodyCell(payment.amount.toString(), 1, columnWidths)
                BodyCell(payment.title ?: "", 2, columnWidths)
                BodyCell(payment.method, 3, columnWidths)
                BodyCell(payment.forInvoices.toString(), 4, columnWidths)
                BodyCell(5, columnWidths) {
                    IconButton(onClick = {
                        viewModel.createConfirmationModal(
                            ModalData(
                                onDismissRequest = { viewModel.closeConfirmationModal() },
                                onConfirmation = {
//                                    viewModel.deletePayment(payment.id.toString())
                                    viewModel.deletePaymentAndRefreshClient(
                                        payment.id.toString(),
                                        payment.fromClient?.id
                                    )
                                },
                                dialogTitle = "Wyślij ponownie",
                                dialogText = "Czy usunąć płatność z dnia: ${payment.date}?",
                                icon = Icons.Default.Delete
                            )
                        )
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "delete")
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCell(text: String, index: Int, columnWidths: MutableList<Int>) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(4.dp)
            .then(sharedWidthModifier(index, columnWidths))
    )
}

@Composable
fun BodyCell(text: String?, index: Int, columnWidths: MutableList<Int>) {
    if (text != null) {
        Text(
            text,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(4.dp)
                .then(sharedWidthModifier(index, columnWidths))
        )
    }
}

@Composable
fun BodyCell(index: Int, columnWidths: MutableList<Int>, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .then(sharedWidthModifier(index, columnWidths))
    ) {
        content()
    }
}

/**
 * Modyfikator, który zapewnia wspólną szerokość kolumny
 */

@Composable
fun sharedWidthModifier(
    index: Int,
    columnWidths: MutableList<Int>
): Modifier {
    val density = LocalDensity.current

    if (columnWidths.size <= index) {
        repeat(index - columnWidths.size + 1) {
            columnWidths.add(0)
        }
    }

    return Modifier
        .onGloballyPositioned { coordinates ->
            val width = coordinates.size.width
            if (width > columnWidths[index]) {
                columnWidths[index] = width
            }
        }
        .width(with(density) { columnWidths[index].toDp() })
}

//@Preview(showBackground = true)
//@Composable
//fun PaymentsDetailsPreview() {
//    val viewModel = ParkingAppViewModel()
//    val Payment1 = Payment(
//        amount = BigDecimal.valueOf(350),
//        date = LocalDate.now(),
//        fromClient = Client(),
//        method = PaymentMethod.TRANSFER.name,
//        toAccount = SellerAccount.BUSSINESS,
//        fromAccount = "000000000000000000",
//        title = null,
//        forInvoices = listOf()
//    )
//    val Payment2 = Payment(
//        amount = BigDecimal.valueOf(700),
//        date = LocalDate.now().minus(14, DateTimeUnit.DAY),
//        fromClient = Client(),
//        method = PaymentMethod.TRANSFER.name,
//        toAccount = SellerAccount.BUSSINESS,
//        fromAccount = "1111111111111111111111111",
//        title = null,
//        forInvoices = listOf()
//    )
//    val payments: List<Payment> = listOf(Payment1, Payment2)
//    viewModel.updateInvoicesAndPayments(listOf(), payments)
//
//    PaymentsDetails(
//        viewModel = viewModel
//    )
//}