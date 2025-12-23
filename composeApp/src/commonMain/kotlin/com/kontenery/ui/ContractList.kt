package com.example.parkingandroidview.ui

//import android.icu.math.BigDecimal
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kontenery.ui.AddContract
import com.kontenery.ui.ProductIcon
import com.kontenery.library.model.Contract
import com.kontenery.model.Client
import com.kontenery.model.enums.now
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.service.to2Decimals
import kotlinx.datetime.LocalDate

@Composable
fun ContractList(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val contractOn: Long? = viewModel.state.collectAsState().value.clientNavRow
    val client: Client? = viewModel.state.collectAsState().value.client
    val contracts: List<Contract> = viewModel.state.collectAsState().value.contracts

    Column(modifier = modifier
        .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                , horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Umowy dla: ")
                Text(client?.getName() ?: "Brak danych") }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                , horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Typ: ")
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text("Produkt: ")
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text("Cena: ")
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text("Aktywna: ")
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        }
        LazyColumn(
            Modifier.fillMaxWidth(),
        ) {
            items(contracts) { contract ->
                Column {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = {
                                viewModel.toContractMenu(contract.id)
                            })
                            .fillMaxWidth()
                            .padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProductIcon(contract.product)
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Text(contract.product?.name.toString())
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        contract.netPrice?.to2Decimals()?.let { Text(it) }
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        IsOngoing(contract)
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    }
                    AnimatedVisibility(
                        visible = contract.id == contractOn,
                        modifier = Modifier.padding(horizontal = 5.dp)

                    ) {
    //                    ClientNavRow(client.id, viewModel, modifier)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.padding(4.dp))
            Spacer(Modifier.padding(4.dp))
            Spacer(Modifier.padding(4.dp))
            Spacer(Modifier.padding(4.dp))
            Spacer(Modifier.padding(4.dp))
            Text("Netto: ${sumOfContracts(contracts)}")
            Text("Suma: ${sumOfContracts(contracts, true)}")
            Spacer(Modifier.padding(4.dp))
        }
        AddContract(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth(),
            client = client,
            product = null
        )
    }
}

private fun sumOfContracts(contracts: List<Contract>, isGross: Boolean = false): String {
    var sum: Double = 0.00
    for (contract in contracts) {
        if(isGross) {
            val price: Double = contract.netPrice?.plus((contract.netPrice!! * (contract.vatRate) / 100.00)) ?: 0.00
            sum = sum.plus(price)
        } else sum = sum.plus(contract.netPrice ?: 0.00)
    }
    return sum.to2Decimals()
}

@Composable
fun ContractNavRow(
    contractId: Long,
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier
        .fillMaxWidth()
        , horizontalArrangement = Arrangement.SpaceAround
    ) {
        Button(onClick = { /*TODO*/ }) {
            Text("Edytuj")
        }
        Button(onClick = { /*TODO*/ }) {
            Text("Usuń")
        }
    }
}

@Composable
fun IsOngoing(contract: Contract) {
    if(
        contract.endDate != null
        && (contract.endDate!! < LocalDate.now())
    ) {
        Icon(
            Icons.Default.Clear
            , contentDescription = "Umowa wygasła: ${contract.endDate}"
            , tint = Color.Red
        )
    } else {
        Icon(
            Icons.Default.Done
            , contentDescription = "Umowa aktywna"
            , tint = Color.Green
        )
    }
}

//@Preview(showBackground = true, widthDp = 500 )
//@Composable
//fun ContractListPreview() {
//    val viewModel = ParkingAppViewModel()
//    val client: Client = Client(
//        id = 1,
//        clientPrivate = ClientPersonalData(
//            id = 101,
//            firstName = "Alice",
//            lastName = "Smith",
//            pesel = "12345678901",
//            passport = "AB1234567",
//            address = Address(null, "Main St", "123", "New York", "10001"),
//            phone = "+1 555-1234",
//            email = "alice.smith@example.com"
//        ),
//        isActive = true
//    )
//    // Sample Clients
//
//    val sampleClients = listOf(
//        // Company Clients
//        Client(
//            id = 1,
//            clientCompany = ClientCompanyData(
//                id = 1,
//                name = "Acme Corp",
//                nip = "1234567890",
//                krs = "0000123456",
//                address = Address(6, "Industrial St 5", "Warsaw", "00-100", "Poland"),
//                phone = "+48 500 100 200",
//                email = "contact@acme.com",
//                needInvoice = true
//            ),
//            isActive = true,
//            createdAt = LocalDate(2022, 1, 5),
//            updatedAt = LocalDate(2023, 6, 10),
//            invoiceTitle = "ACME CORP SP. Z O.O.",
//            bankAccounts = listOf("PL60102010260000042270201111")
//        ),
//        Client(
//            id = 2,
//            clientCompany = ClientCompanyData(
//                id = 2,
//                name = "Global Logistics",
//                nip = "9876543210",
//                krs = "0000654321",
//                address = Address(5, "Logistics Ave 42", "Gdańsk", "80-500", "Poland"),
//                phone = "+48 600 200 300",
//                email = "info@globallogistics.pl",
//                needInvoice = true
//            ),
//            isActive = true,
//            createdAt = LocalDate(2021, 9, 12),
//            updatedAt = LocalDate(2023, 8, 22),
//            invoiceTitle = "GLOBAL LOGISTICS S.A.",
//            bankAccounts = listOf("PL27114020040000310234567890")
//        ),
//        Client(
//            id = 3,
//            clientCompany = ClientCompanyData(
//                id = 3,
//                name = "BuildIt Ltd",
//                nip = "5432167890",
//                krs = "0000789012",
//                address = Address(4, "Construction Rd 10", "Kraków", "30-300", "Poland"),
//                phone = "+48 700 300 400",
//                email = "office@buildit.com",
//                needInvoice = false
//            ),
//            isActive = false,
//            createdAt = LocalDate(2020, 5, 15),
//            updatedAt = LocalDate(2023, 5, 5),
//            invoiceTitle = "BUILD IT LTD",
//            bankAccounts = listOf("PL92102055580000201345678912")
//        ),
//
//        // Private Clients
//        Client(
//            id = 4,
//            clientPrivate = ClientPersonalData(
//                id = 4,
//                firstName = "Anna",
//                lastName = "Kowalska",
//                pesel = "85031212345",
//                passport = "X1234567",
//                address = Address(3,"Maple St 7", "Poznań", "60-101", "Poland"),
//                phone = "+48 511 111 222",
//                email = "anna.kowalska@example.com"
//            ),
//            isActive = true,
//            createdAt = LocalDate(2023, 3, 1),
//            updatedAt = LocalDate(2023, 7, 20),
//            bankAccounts = listOf("PL16114010100000201345678901")
//        ),
//        Client(
//            id = 5,
//            clientPrivate = ClientPersonalData(
//                id = 5,
//                firstName = "Piotr",
//                lastName = "Nowak",
//                pesel = "92041509876",
//                passport = "Y7654321",
//                address = Address(1, "Oak Lane 12", "Wrocław", "50-200", "Poland"),
//                phone = "+48 522 222 333",
//                email = "piotr.nowak@example.com"
//            ),
//            isActive = false,
//            createdAt = LocalDate(2022, 6, 15),
//            updatedAt = LocalDate(2023, 2, 14),
//            bankAccounts = listOf("PL55102010260000042270202222")
//        ),
//        Client(
//            id = 6,
//            clientPrivate = ClientPersonalData(
//                id = 6,
//                firstName = "Katarzyna",
//                lastName = "Wiśniewska",
//                pesel = "95072101234",
//                passport = "Z0987654",
//                address = Address(2,"Pine St 3", "Łódź", "90-400", "Poland"),
//                phone = "+48 533 333 444",
//                email = "katarzyna.wisniewska@example.com"
//            ),
//            isActive = true,
//            createdAt = LocalDate(2021, 11, 10),
//            updatedAt = LocalDate(2023, 6, 1),
//            bankAccounts = listOf("PL11102010260000042270203333")
//        )
//    )
//
//// Use sampleContainers & sampleYards defined earlier
//    val sampleProductsAll = listOf(
//        // --- CONTAINERS ---
//        Product.Container(
//            id = 1,
//            name = "Blue Shipping Container",
//            location = "Warehouse A",
//            client = sampleClients[0], // Acme Corp
//            length = "6m",
//            height = "2.5m",
//            color = "Blue",
//            acquireDate = LocalDate(2021, 5, 10),
//            lastPainting = LocalDate(2023, 4, 1),
//            description = "Used for storing construction materials"
//        ),
//        Product.Container(
//            id = 2,
//            name = "Green Storage Container",
//            location = "Dock 3",
//            client = sampleClients[1], // Global Logistics
//            length = "12m",
//            height = "2.9m",
//            color = "Green",
//            acquireDate = LocalDate(2020, 3, 14),
//            lastPainting = LocalDate(2022, 8, 15),
//            description = "Refrigerated container for perishables"
//        ),
//        Product.Container(
//            id = 3,
//            name = "Red Transport Container",
//            location = "Yard Section B",
//            client = sampleClients[2], // BuildIt Ltd
//            length = "6m",
//            height = "2.6m",
//            color = "Red",
//            acquireDate = LocalDate(2019, 11, 25),
//            lastPainting = LocalDate(2023, 1, 10),
//            description = "Container used for heavy equipment transport"
//        ),
//
//        // --- YARDS ---
//        Product.Yard(
//            id = 4,
//            name = "Main Storage Yard",
//            location = "South Wing",
//            client = sampleClients[3], // Anna Kowalska
//            quantity = 1500L
//        ),
//        Product.Yard(
//            id = 5,
//            name = "Secondary Yard",
//            location = "North Section",
//            client = sampleClients[4], // Piotr Nowak
//            quantity = 800L
//        ),
//        Product.Yard(
//            id = 6,
//            name = "Temporary Yard",
//            location = "East Wing",
//            client = sampleClients[5], // Katarzyna Wiśniewska
//            quantity = 400L
//        )
//    )
//
//// Sample Contracts
//    val sampleContracts = listOf(
//        Contract(
//            id = 1,
//            client = sampleClients[0],
//            product = sampleProductsAll[0],
//            startDate = LocalDate(2023, 1, 10),
//            endDate = LocalDate(2023, 12, 31),
//            netPrice = BigDecimal("1200.00"),
//            vatRate = BigDecimal("23"),
//            needInvoice = true
//        ),
//        Contract(
//            id = 2,
//            client = sampleClients[1],
//            product = sampleProductsAll[1],
//            startDate = LocalDate(2023, 3, 1),
//            endDate = LocalDate(2024, 3, 1),
//            netPrice = BigDecimal("850.50"),
//            vatRate = BigDecimal("23"),
//            needInvoice = false
//        ),
//        Contract(
//            id = 3,
//            client = sampleClients[2],
//            product = sampleProductsAll[2],
//            startDate = LocalDate(2022, 5, 20),
//            endDate = LocalDate(2023, 5, 20),
//            netPrice = BigDecimal("2200.00"),
//            vatRate = BigDecimal("8"),
//            needInvoice = true
//        ),
//        Contract(
//            id = 4,
//            client = sampleClients[0],
//            product = sampleProductsAll[3],
//            startDate = LocalDate(2023, 6, 15),
//            endDate = LocalDate(2024, 6, 15),
//            netPrice = BigDecimal("1400.00"),
//            vatRate = BigDecimal("23"),
//            needInvoice = true
//        ),
//        Contract(
//            id = 5,
//            client = sampleClients[1],
//            product = sampleProductsAll[4],
//            startDate = LocalDate(2023, 9, 1),
//            endDate = LocalDate(2025, 9, 1),
//            netPrice = BigDecimal("500.00"),
//            vatRate = BigDecimal("23"),
//            needInvoice = false
//        ),
//        Contract(
//            id = 6,
//            client = sampleClients[2],
//            product = sampleProductsAll[5],
//            startDate = LocalDate(2023, 11, 5),
//            endDate = LocalDate(2024, 11, 5),
//            netPrice = BigDecimal("750.75"),
//            vatRate = BigDecimal("8"),
//            needInvoice = true
//        ),
//        Contract(
//            id = 7,
//            client = sampleClients[0],
//            product = sampleProductsAll[0],
//            startDate = LocalDate(2024, 1, 1),
//            endDate = LocalDate(2024, 12, 31),
//            netPrice = BigDecimal("999.99"),
//            vatRate = BigDecimal("23"),
//            needInvoice = true
//        ),
//        Contract(
//            id = 8,
//            client = sampleClients[1],
//            product = sampleProductsAll[1],
//            startDate = LocalDate(2023, 2, 15),
//            endDate = LocalDate(2023, 10, 15),
//            netPrice = BigDecimal("300.00"),
//            vatRate = BigDecimal("8"),
//            needInvoice = false
//        ),
//        Contract(
//            id = 9,
//            client = sampleClients[2],
//            product = sampleProductsAll[2],
//            startDate = LocalDate(2023, 7, 10),
//            endDate = LocalDate(2024, 7, 10),
//            netPrice = BigDecimal("1750.00"),
//            vatRate = BigDecimal("23"),
//            needInvoice = true
//        ),
//        Contract(
//            id = 10,
//            client = sampleClients[0],
//            product = sampleProductsAll[3],
//            startDate = LocalDate(2022, 8, 1),
//            endDate = LocalDate(2023, 8, 1),
//            netPrice = BigDecimal("420.00"),
//            vatRate = BigDecimal("8"),
//            needInvoice = false
//        )
//    )
//    val state = ParkingAppState()
//    viewModel.setState(state.copy(
//        contracts = sampleContracts,
//        client = sampleClients[0],
//    ))
//    ContractList(viewModel, modifier = Modifier)
//}