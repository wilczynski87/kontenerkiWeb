package com.kontenery.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kontenery.model.invoice.Invoice
import com.kontenery.model.enums.now
import com.kontenery.model.invoice.Position
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.service.isDigitsOnly
import com.kontenery.service.to2Decimals
import kotlinx.datetime.LocalDate

@Composable
fun InvoiceForm(
    viewModel: ParkingAppViewModel
    , modifier: Modifier = Modifier
        .fillMaxSize()
) {
    val state by viewModel.state.collectAsState()
    val invoice: Invoice = state.invoice ?: return
    val clients = state.clients
    val client = state.client
    var expandedClients by remember { mutableStateOf(false) }

    // set default customer to invoice
    LaunchedEffect(Unit) {
        if (client?.id != null)
            client.id.let {
                viewModel.ensureInvoiceCustomer()
            }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier.fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("Wystaw nieokresową fakturę:")
        }
        Row() {
            OutlinedCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Text("Klient: ",
                    modifier = Modifier.padding(4.dp))
                ClientsDropdown(
                    chosenClient = client,
                    selectClient = { selectedClient ->
                        viewModel.updateCustomerToInvoice(selectedClient)
//                        println(
//                            "ClientsDropdown selectedClient: $selectedClient, viewModel: $invoice"
//                        )
                    },
                    clients = clients,
                    expanded = expandedClients,
                    toggleExpanded = { expandedClients = !expandedClients },
                )
            }
        }
        Row() {
            BillType(
                needInvoice = client?.needInvoice() == true,
                enabled = false,
                toggleInvoice = { needInvoice: Boolean ->
                    viewModel.sellerForInvoiceUpdate()
                }
            )
        }
        Row() {
            ChooseDate(
                title = "Data wystawienia",
                date = LocalDate.now(),
                updateDate = { date ->
                    viewModel.updateInvoice(invoice.copy(invoiceDate = LocalDate.parse(date)))
                }
            )
        }
        Row() {
            OutlinedTextField(
                value = invoice.invoiceTitle ?: "",
                onValueChange = {
                    viewModel.updateInvoice(invoice.copy(invoiceTitle = it))
                },
                label = { Text("Tytuł faktury: ") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row {
            ProductsTable(viewModel)
        }

        Row() {
            Button(
                onClick = {
                    // TODO jak nie ma id customera to jakiś błąd?
                    val invoice: Invoice = invoice
                    viewModel.postCustomInvoice(client?.id ?: 0, invoice)
                    viewModel.showConfirmModal(
                        "Status dodatkowej faktury",
                        "Wysyłam fakturę do klienta",
                        viewModel::closeConfirmationModal,
                    )
                },
            ) {
                Text("Wyślij dodatkową fakturę")
            }
        }
        Row {
            ProductForm(viewModel)
        }
    }
}

// TODO scrolling albo coś?
@Composable
fun ProductsTable(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .defaultMinSize(minWidth = 600.dp)
) {
    val state by viewModel.state.collectAsState()
    val positions: List<Position> = state.invoice?.products ?: listOf()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
//        println("positions: $positions")
        val sumPrice = positions.sumOf { it.price?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 }
        val sumVat = positions.sumOf { it.vatAmount?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 }
        val sumWithVat = positions.sumOf { it.priceWithVat?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 }
//        println("sumPrice: $sumPrice")
//        println("sumVat: $sumVat")
//        println("sumWithVat: $sumWithVat")

        val columnWeight = listOf(2f, 1f, 1f, 1f, 1f, 1f, 1.5f, 1f)

        // Table Header
        TableRow(
            columnWeight,
            listOf("Produkt:", "jed.:", "ilość:", "netto:", "VAT%", "VAT", "brutto:", "usuń:"),
            isHeader = true,
            viewModel = viewModel
        )

        // Table Rows
        positions.forEachIndexed { index, position ->
            TableRow(
                columnWeight,
                listOf(
                    position.productName.orEmpty(),
                    position.unitPrice.orEmpty(),
                    position.quantity.orEmpty(),
                    position.price.orEmpty(),
                    position.vatRate.orEmpty(),
                    position.vatAmount.orEmpty(),
                    position.priceWithVat.orEmpty(),
                ),
                viewModel = viewModel,
                index = index
            )
        }

        // Summary Row
        TableRow(
            columnWeight,
            listOf("Suma:", "", "", sumPrice.to2Decimals(), "", sumVat.to2Decimals(), sumWithVat.to2Decimals(), ""),
            isSummary = true,
            viewModel = viewModel
        )
    }
}

@Composable
fun TableRow(
    weights: List<Float>,
    values: List<String>,
    isHeader: Boolean = false,
    isSummary: Boolean = false,
    viewModel: ParkingAppViewModel,
    index: Int? = null
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        values.forEachIndexed { colIndex, value ->
            Text(
                text = value,
                modifier = Modifier
                    .weight(weights.getOrElse(colIndex) { 1f })
                    .padding(4.dp),
                fontWeight = when {
                    isHeader || isSummary -> FontWeight.Bold
                    else -> FontWeight.Normal
                },
                fontSize = if (isHeader) 14.sp else 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSummary.not() && isHeader.not() && index != null) {
            TextButton(
                onClick = {
                    viewModel.removeProductFromInvoice(index)
                }
            ) { Icon(Icons.Default.Clear, "usuń pozycje") }
        }
    }
}

@Composable
fun ProductForm(
    viewModel: ParkingAppViewModel
) {
    val state by viewModel.state.collectAsState()
    var position: Position? = state.position ?: Position()
    LaunchedEffect(Unit) {
        viewModel.updatePosition(null)
    }

//    positions.forEachIndexed { index, position ->
    Column (
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Dodaj produkt: "
            , fontStyle = MaterialTheme.typography.headlineLarge.fontStyle
        )
        OutlinedTextField(
            value = position?.productName ?: "",
            onValueChange = {
                    viewModel.updatePosition(position?.copy(productName = it))
            },
            label = { Text("Nazwa: ") },
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            OutlinedTextField(
                value = position?.unitPrice ?: "",
                onValueChange = {
                    if(it.toDoubleOrNull() != null) {
                        val num = it.toDoubleOrNull() ?: 0.00
                        viewModel.calculatePosition(position!!.copy(unitPrice = num.toString()))
                    } else {
                        println("ProductForm nie udało się zamienić na double")
                    }
                    // wywal jakiś błąd? - jeśli nie da się zamienić na double

                },
                label = { Text("Cena jednostkowa: ") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = position?.quantity ?: "",
                onValueChange = {
                    if(it.toDoubleOrNull() != null)
                    viewModel.calculatePosition(position!!.copy(quantity = it))
                },
                label = { Text("Ilość: ") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = position?.price ?: "",
            onValueChange = {
                if(it.toDoubleOrNull() != null) {
                    viewModel.calculatePosition(position!!.copy(price = it))
                }
            },
            label = { Text("Cena netto: ") },
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            OutlinedTextField(
                value = position?.vatRate ?: "",
                onValueChange = {
                    if(it.isDigitsOnly())
                    viewModel.calculatePosition(position!!.copy(vatRate = it))
                },
                label = { Text("Stawka VAT: ") },
                modifier = Modifier.weight(0.5f)
            )
            OutlinedTextField(
                value = position?.vatAmount ?: "",
                onValueChange = {
                    if(it.toDoubleOrNull() != null) {
//                        val num = String.format("%.2f", it.toDouble())
//                        val num = String.format(java.util.Locale.getDefault(), "%.2f", it.toDouble())
                        viewModel.updatePosition(position!!.copy(vatAmount = it))
                    }
                },
                label = { Text("VAT: ") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = position?.priceWithVat ?: "",
            onValueChange = {
                if(it.toDoubleOrNull() != null) {
//                    val num = String.format("%.2f", it.toDouble())
                    viewModel.updatePosition(position!!.copy(priceWithVat = it))
                }
            },
            label = { Text("Cena brutto: ") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
            , horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                { viewModel.updatePosition(null) }
            ) {
                Text("Nowy produkt")
            }
            Button(
                onClick = {
                    viewModel.addProductToInvoice()
                }
            ) {
                Text("Dodaj produkt:")
            }
        }
    }
}
//
//@Preview(showBackground = true, widthDp = 500 )
//@Composable
//fun PreviewInviceForm() {
//    val viewModel = ParkingAppViewModel()
//    var position: Position = Position(
//        productName = "Test Produkt 1 - marzec",
//        unitPrice = "150",
//        quantity = "3",
//        price = null,
//        vatRate = null,
//        vatAmount = null,
//        priceWithVat = null,
//    )
//    viewModel.calculatePosition(position)
//    viewModel.updatePosition(position)
//
//    InvoiceForm(viewModel)
//}