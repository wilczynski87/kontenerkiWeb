package com.kontenery.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.library.model.Address
import com.kontenery.model.Client
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientPersonalData
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.ParkingAppState
import com.kontenery.service.ParkingAppViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientBox(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val state: ParkingAppState by viewModel.state.collectAsState()
    val client: Client? = state.client
    val clientPrv: ClientPersonalData = client?.clientPrivate ?: ClientPersonalData()
    val company: ClientCompanyData = client?.clientCompany ?: ClientCompanyData()
    val isActive: Boolean = client?.isActive == true

    if(windowSize == WindowWidthSizeClass.Compact) {
        Column(modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if(client == null) {
                    LoadingBox("dane klienta")
                } else {
                    PersonalData(
                        viewModel = viewModel,
                        clientPrv = clientPrv,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CompanyData(
                        viewModel = viewModel,
                        company = company,
                        windowSize = windowSize,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IsActiveForm(
                        isActive = isActive,
                        viewModel = viewModel,
                        windowSize = windowSize,
                        modifier = Modifier.weight(1f)
                    )
                    AddBankAccount(
                        viewModel = viewModel,
                        Modifier.weight(1f)
                    )
                }
            }
            SubmitClientFormButton(
                client,
                viewModel,
                modifier = Modifier.weight(1f)
//                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    } else {
        if(client == null) {
            LoadingBox("dane klienta")
        } else {
            Column(modifier = modifier
                .padding(1.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                ,
            ) {
                Row(
                    modifier = Modifier
                        .padding(1.dp)
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    PersonalData(
                        viewModel = viewModel,
                        clientPrv = clientPrv,
                        modifier = Modifier.weight(1f),
                    )
                    CompanyData(
                        viewModel = viewModel,
                        company = company,
                        windowSize = windowSize,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row (
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val accounts = client.bankAccounts.orEmpty()
                            if(accounts.isEmpty()) {
                                Text("Brak kont bankowych...")
                            }
                            accounts.forEach { account ->
                                Text(account)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row {
                            AddBankAccount(
                                viewModel = viewModel,
                                modifier = Modifier
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IsActiveForm(
                        isActive,
                        viewModel = viewModel,
                        windowSize = windowSize,
                        modifier = Modifier
                            .weight(1f)
                    )
                    SubmitClientFormButton(
                        client,
                        viewModel,
                        modifier = Modifier
                            .weight(1f)
                    )

                }
            }
        }
    }
}

@Composable
fun PersonalData(
    viewModel: ParkingAppViewModel,
    clientPrv: ClientPersonalData,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier
        .padding(4.dp)
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small ,
        )
    ) {
        Text(
            "Dane klienta: "
            , modifier = Modifier.padding(8.dp)
            , style = MaterialTheme.typography.titleLarge
        )
        val fieldModifier: Modifier = Modifier.padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 0.dp)
        OutlinedTextField(
            value = clientPrv.firstName ?: ""
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(firstName = it)) }
            , label = { Text("Imię:") }
            , modifier = fieldModifier
        )
        OutlinedTextField(
            value = clientPrv.lastName ?: ""
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(lastName = it)) }
            , label = { Text("Nazwisko:") }
            , modifier = fieldModifier
        )
        OutlinedTextField(
            value = clientPrv.pesel ?: ""
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(pesel = it)) }
            , label = { Text("Pesel:") }
            , modifier = fieldModifier
        )
        OutlinedTextField(
            value = clientPrv.passport ?: ""
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(passport = it)) }
            , label = { Text("Nr. Paszportu:") }
            , modifier = fieldModifier
        )
        OutlinedTextField(
            value = clientPrv.email ?: ""
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(email = it)) }
            , label = { Text("Em@il:") }
            , modifier = fieldModifier
        )
        OutlinedTextField(
            value = clientPrv.phone ?: ""
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(phone = it)) }
            , label = { Text("Telefon:") }
            , modifier = fieldModifier
        )
        OutlinedTextField(
            value = clientPrv.salutation
            , onValueChange = { viewModel.updateClientPersonalData(clientPrv.copy(salutation = it)) }
            , label = { Text("Zwrot grzwecznościowy:") }
            , modifier = fieldModifier
        )
        AddressData(
            address = clientPrv.address,
            updateAddress = { address -> viewModel.updateClientPersonalData(clientPrv.copy(address = address)) },
            modifier = fieldModifier.padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CompanyData(
    viewModel: ParkingAppViewModel,
    company: ClientCompanyData,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .padding(4.dp)
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small ,
        )
    ) {
        Text(
            "Firma: "
            , modifier = Modifier.padding(8.dp)
            , style = MaterialTheme.typography.titleLarge
        )
        val modifier: Modifier = Modifier.padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 0.dp)
        OutlinedTextField(
            value = company.name ?: ""
            , onValueChange = { viewModel.updateClientCompanyData(company.copy(name = it)) }
            , label = { Text("Firma:") }
            , modifier = modifier
        )
        OutlinedTextField(
            value = company.nip ?: ""
            , onValueChange = { viewModel.updateClientCompanyData(company.copy(nip = it)) }
            , label = { Text("NIP:") }
            , modifier = modifier
        )
        OutlinedTextField(
            value = company.krs ?: ""
            , onValueChange = { viewModel.updateClientCompanyData(company.copy(krs = it)) }
            , label = { Text("KRS:") }
            , modifier = modifier
        )
        OutlinedTextField(
            value = company.phone ?: ""
            , onValueChange = { viewModel.updateClientCompanyData(company.copy(phone = it)) }
            , label = { Text("Tel:") }
            , modifier = modifier
        )
        OutlinedTextField(
            value = company.email ?: ""
            , onValueChange = { viewModel.updateClientCompanyData(company.copy(email = it)) }
            , label = { Text("Em@il:") }
            , modifier = modifier
        )
        NeedInvoiceSwitch(
            company.needInvoice == true,
            { viewModel.needInvoiceToggle() },
            windowSize = windowSize,
            modifier
        )
        AddressData(
            address = company.address,
            updateAddress = { address -> viewModel.updateClientCompanyData(company.copy(address = address)) },
            modifier = modifier.padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun NeedInvoiceSwitch(
    payVat: Boolean,
    payVatSwitch: () -> Any,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if(windowSize == WindowWidthSizeClass.Compact) Arrangement.SpaceBetween else Arrangement.Start,
    ) {
        Text("Chce fakturę?")
        Switch(
            checked = payVat,
            onCheckedChange = { payVatSwitch() }
        )
    }
}

@Composable
fun AddressData(
    address: Address?,
    updateAddress: (Address) -> Unit,
    modifier: Modifier = Modifier
) {
    val address = address ?: Address()
    Text(
        text = "Adres: ",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )

    OutlinedTextField(
        value = address.street ?: ""
        , onValueChange = { updateAddress(address.copy(street = it)) }
        , label = { Text("Ulica:") }
        , modifier = modifier
    )
    OutlinedTextField(
        value = address.house ?: ""
        , onValueChange = { updateAddress(address.copy(house = it)) }
        , label = { Text("Numer:") }
        , modifier = modifier
    )
    OutlinedTextField(
        value = address.city ?: ""
        , onValueChange = { updateAddress(address.copy(city = it)) }
        , label = { Text("Miasto:") }
        , modifier = modifier
    )
    OutlinedTextField(
        value = address.postCode ?: ""
        , onValueChange = { updateAddress(address.copy(postCode = it)) }
        , label = { Text("Kod pocztowy:") }
        , modifier = modifier
    )
    OutlinedTextField(
        value = address.country
        , onValueChange = { updateAddress(address.copy(country = it)) }
        , label = { Text("Kraj:") }
        , modifier = modifier
    )
}

@Composable
fun IsActiveForm(
    isActive: Boolean,
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
//            .fillMaxWidth()
            .padding(1.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Aktywny: "
            , modifier = Modifier.padding(0.dp)
            , style = MaterialTheme.typography.titleLarge
        )
        Switch(
            checked = isActive,
            onCheckedChange = {
                viewModel.isActiveClientToggle()
            },
            modifier = Modifier.padding(0.dp)
        )
    }

}

@Composable
fun SubmitClientFormButton(
    client: Client?,
    viewModel: ParkingAppViewModel
    , modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(1.dp)
    ) {
        if(client != null && client.id != null) {
            UpdateClientFormButton(viewModel, modifier = Modifier, client)
        } else {
            SaveClientFormButton(viewModel, modifier = Modifier, client)
        }
    }
}

@Composable
fun SaveClientFormButton(
    viewModel: ParkingAppViewModel
    , modifier: Modifier = Modifier
    , client: Client? = null
) {
    Button(onClick = {
        if(client != null) viewModel.saveClient(client)
        viewModel.toClientList()
    }
        , modifier = Modifier
    ) {
        Text("Zapisz")
    }
}

@Composable
fun UpdateClientFormButton(
    viewModel: ParkingAppViewModel
    , modifier: Modifier = Modifier
    , client: Client? = null
) {
    Button(onClick = {
        if(client != null) viewModel.putClient()
        viewModel.toClientList()
    }
        , modifier = modifier
    ) {
        Text("Uaktualnij")
    }
}

@Composable
fun AddBankAccount(
    viewModel: ParkingAppViewModel
    , modifier: Modifier = Modifier
) {
    Button(onClick = {
            viewModel.toBankAccountMenu()
        }
    ) {
        Text("Dodaj konto")
    }
}

//@Preview(showBackground = true, widthDp = 340 )
//@Composable
//fun PreviewClientBoxCompact() {
//    val personalClient1 = ClientPersonalData(
//        id = 1L,
//        firstName = "John",
//        lastName = "Doe",
//        pesel = "12345678901",
//        passport = "AB1234567",
//        address = Address(
//            street = "Main St",
//            house = "10A",
//            city = "Warsaw",
//            postCode = "00-001",
//            country = "PL"
//        ),
//        phone = "+48 123 456 789",
//        email = "john.doe@example.com"
//    )
//    val client: Client = Client(null, personalClient1, clientCompany = ClientCompanyData(), true)
//    val viewModel = ParkingAppViewModel()
//    viewModel.updateClient(client)
//    val windowSize = WindowWidthSizeClass.Compact
//
//    ClientBox(
//        viewModel = viewModel,
//        windowSize,
//    )
//}

//@Preview
//@Composable
//fun PreviewClientBoxCompact() {
//    val personalClient1 = ClientPersonalData(
//        id = 1L,
//        firstName = "John",
//        lastName = "Doe",
//        pesel = "12345678901",
//        passport = "AB1234567",
//        address = Address(
//            street = "Main St",
//            house = "10A",
//            city = "Warsaw",
//            postCode = "00-001",
//            country = "PL"
//        ),
//        phone = "+48 123 456 789",
//        email = "john.doe@example.com"
//    )
//    val client = Client(null, personalClient1, clientCompany = ClientCompanyData(), null)
//    val viewModel = ParkingAppViewModel()
//    viewModel.updateClient(client)
//    val windowSize = WindowWidthSizeClass.Compact
////    val windowSize = WindowWidthSizeClass.Medium
//    ClientBox(
//        viewModel = viewModel,
//        windowSize,
//    )
//}

//@Preview(showBackground = true, widthDp = 600 )
//@Composable
//fun PreviewClientBoxExpanded() {
//    val personalClient1 = ClientPersonalData(
//        id = 1L,
//        firstName = "John",
//        lastName = "Doe",
//        pesel = "12345678901",
//        passport = "AB1234567",
//        address = Address(
//            street = "Main St",
//            houseNumber = "10A",
//            city = "Warsaw",
//            zipCode = "00-001",
//            country = "PL"
//        ),
//        phone = "+48 123 456 789",
//        email = "john.doe@example.com"
//    )
//    val client: Client = Client(null, personalClient1, clientCompany = ClientCompanyData(), true)
//    val viewModel = ParkingAppViewModel()
//    viewModel.updateClient(client)
//    val windowSize = WindowWidthSizeClass.Medium
//
//    ClientBox(
//        viewModel = viewModel,
//        windowSize,
//    )
//}