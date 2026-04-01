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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.library.model.Address
import com.kontenery.model.Client
import com.kontenery.model.ClientCompanyData
import com.kontenery.model.ClientEvent
import com.kontenery.model.ClientPersonalData
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.ParkingAppViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientBox(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val client = state.client

    if (client == null) {
        LoadingBox("dane klienta")
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // globalny scroll
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ClientScreen(viewModel, windowSize, client)
    }
}

@Composable
fun ClientScreen(
    viewModel: ParkingAppViewModel,
    windowSize: WindowWidthSizeClass,
    client: Client
) {
    when (windowSize) {
        WindowWidthSizeClass.Compact -> ClientFormSingleColumn(viewModel, client)
        WindowWidthSizeClass.Medium -> ClientFormTwoColumns(viewModel, client)
        WindowWidthSizeClass.Expanded -> ClientFormWithSidebar(viewModel, client)
    }
}

@Composable
fun ClientFormSingleColumn(viewModel: ParkingAppViewModel, client: Client) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        client.clientPrivate?.let { PersonalData(viewModel, it) }
        client.clientCompany?.let { CompanyData(viewModel, it, WindowWidthSizeClass.Compact) }
        BankAccountsSection(client, viewModel)
        BottomActions(viewModel, client)
    }
}

@Composable
fun ClientFormTwoColumns(viewModel: ParkingAppViewModel, client: Client) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            client.clientPrivate?.let { PersonalData(viewModel, it) }
            BankAccountsSection(client, viewModel)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            client.clientCompany?.let { CompanyData(viewModel, it, WindowWidthSizeClass.Medium) }
            BottomActions(viewModel, client)
        }
    }
}

@Composable
fun ClientFormWithSidebar(viewModel: ParkingAppViewModel, client: Client) {
    var selectedTab by remember { mutableStateOf("Klient") }

    Row(modifier = Modifier.fillMaxSize()) {
        // SIDEBAR
        NavigationRail {
            NavigationRailItem(
                selected = selectedTab == "Klient",
                onClick = { selectedTab = "Klient" },
                icon = { Icon(Icons.Default.Person, null) },
                label = { Text("Klient") }
            )
            NavigationRailItem(
                selected = selectedTab == "Firma",
                onClick = { selectedTab = "Firma" },
                icon = { Icon(Icons.Default.Business, null) },
                label = { Text("Firma") }
            )
        }

        // CONTENT
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                "Klient" -> client.clientPrivate?.let { PersonalData(viewModel, it, modifier = Modifier.weight(1f)) }
                "Firma" -> client.clientCompany?.let { CompanyData(viewModel, it, WindowWidthSizeClass.Expanded, modifier = Modifier.weight(1f)) }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                BankAccountsSection(client, viewModel)
                BottomActions(viewModel, client)
            }
        }
    }
}

@Composable
fun BottomActions(
    viewModel: ParkingAppViewModel,
    client: Client
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IsActiveForm(
            isActive = client.isActive == true,
            viewModel = viewModel
        )

        Button(onClick = {
            viewModel.onClientEvent(
                if (client.id != null) ClientEvent.Update else ClientEvent.Save
            )
        }) {
            Text(if (client.id != null) "Uaktualnij" else "Zapisz")
        }
    }
}

@Composable
fun PersonalData(
    viewModel: ParkingAppViewModel,
    clientPrv: ClientPersonalData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Dane klienta", style = MaterialTheme.typography.titleLarge)

        TextFieldBlock(
            value = clientPrv.firstName,
            label = "Imię"
        ) {
            viewModel.onClientEvent(ClientEvent.Personal.FirstNameChanged(it))
        }

        TextFieldBlock(clientPrv.lastName, "Nazwisko") {
            viewModel.onClientEvent(ClientEvent.Personal.LastNameChanged(it))
        }

        TextFieldBlock(clientPrv.pesel, "PESEL") {
            viewModel.onClientEvent(ClientEvent.Personal.PeselChanged(it))
        }

        TextFieldBlock(clientPrv.passport, "Paszport") {
            viewModel.onClientEvent(ClientEvent.Personal.PassportChanged(it))
        }

        TextFieldBlock(clientPrv.email, "Email") {
            viewModel.onClientEvent(ClientEvent.Personal.EmailChanged(it))
        }

        TextFieldBlock(clientPrv.phone, "Telefon") {
            viewModel.onClientEvent(ClientEvent.Personal.PhoneChanged(it))
        }

        TextFieldBlock(clientPrv.salutation, "Zwrot") {
            viewModel.onClientEvent(ClientEvent.Personal.SalutationChanged(it))
        }

        AddressData(
            address = clientPrv.address,
            onChange = { viewModel.updateClientPersonalData(clientPrv.copy(address = it)) }
        )
    }
}
@Composable
fun CompanyData(
    viewModel: ParkingAppViewModel,
    company: ClientCompanyData,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Firma", style = MaterialTheme.typography.titleLarge)

        TextFieldBlock(company.name, "Nazwa") {
            viewModel.onClientEvent(ClientEvent.Company.NameChanged(it))
        }

        TextFieldBlock(company.nip, "NIP") {
            viewModel.onClientEvent(ClientEvent.Company.NipChanged(it))
        }

        TextFieldBlock(company.krs, "KRS") {
            viewModel.onClientEvent(ClientEvent.Company.KrsChanged(it))
        }

        TextFieldBlock(company.phone, "Telefon") {
            viewModel.onClientEvent(ClientEvent.Company.PhoneChanged(it))
        }

        TextFieldBlock(company.email, "Email") {
            viewModel.onClientEvent(ClientEvent.Company.EmailChanged(it))
        }

        NeedInvoiceSwitch(
            checked = company.needInvoice == true,
            onToggle = { viewModel.needInvoiceToggle() },
            windowSize = windowSize
        )

        AddressData(
            address = company.address,
            onChange = { viewModel.updateClientCompanyData(company.copy(address = it)) }
        )
    }
}

@Composable
fun BankAccountsSection(
    client: Client,
    viewModel: ParkingAppViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text("Konta bankowe", style = MaterialTheme.typography.titleMedium)

        val accounts = client.bankAccounts.orEmpty()

        if (accounts.isEmpty()) {
            Text("Brak kont")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                accounts.forEach { account ->
                    Text(account)
                }
            }
        }

        AddBankAccount(viewModel)
    }
}

@Composable
fun NeedInvoiceSwitch(
    checked: Boolean,
    onToggle: () -> Unit,
    windowSize: WindowWidthSizeClass
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (windowSize == WindowWidthSizeClass.Compact)
            Arrangement.SpaceBetween else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Faktura?")
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
fun AddressData(
    address: Address?,
    onChange: (Address) -> Unit
) {
    val a = address ?: Address()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Adres", style = MaterialTheme.typography.titleMedium)

        TextFieldBlock(a.street, "Ulica") {
            onChange(a.copy(street = it))
        }
        TextFieldBlock(a.house, "Numer") {
            onChange(a.copy(house = it))
        }
        TextFieldBlock(a.city, "Miasto") {
            onChange(a.copy(city = it))
        }
        TextFieldBlock(a.postCode, "Kod") {
            onChange(a.copy(postCode = it))
        }
        TextFieldBlock(a.country, "Kraj") {
            onChange(a.copy(country = it))
        }
    }
}

@Composable
fun IsActiveForm(
    isActive: Boolean,
    viewModel: ParkingAppViewModel
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Aktywny")
        Switch(
            checked = isActive,
            onCheckedChange = { viewModel.isActiveClientToggle() }
        )
    }
}

@Composable
fun SubmitClientFormButton(
    client: Client,
    viewModel: ParkingAppViewModel
) {
    Button(
        onClick = {
            if (client.id != null) {
                viewModel.putClient()
            } else {
                viewModel.saveClient(client)
            }
            viewModel.toClientList()
        }
    ) {
        Text(if (client.id != null) "Uaktualnij" else "Zapisz")
    }
}

@Composable
fun TextFieldBlock(
    value: String?,
    label: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value ?: "",
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
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