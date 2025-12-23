package com.example.parkingandroidview.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kontenery.model.Client
import com.kontenery.model.ClientBankAccount
import com.kontenery.service.ParkingAppViewModel

@Composable
fun BankAccountMenu(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val client: Client? = viewModel.state.collectAsState().value.client
//    println("bankAccount", "$client")
    val bankAccounts: List<String> = client?.bankAccounts ?: listOf()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Bank account menu",
    //            modyfier = Modifier.padding(16.dp),
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        bankAccounts.forEach {
            BankAccountRow(viewModel, it)
        }
        AccountForm(viewModel)
    }
}

@Composable
fun BankAccountRow(
    viewModel: ParkingAppViewModel,
    accountNumber: String,
    modifier: Modifier = Modifier
) {
    val client: Client = viewModel.state.collectAsState().value.client ?: throw NullPointerException("No Client Number Id")

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding( start = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(accountNumberFormatter(accountNumber.filterNot { it.isWhitespace() }))
        DeleteBankAccount(viewModel, client, accountNumber)
    }
}

@Composable
fun DeleteBankAccount(
    viewModel: ParkingAppViewModel,
    client: Client? = null,
    accountNumber: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            viewModel.deleteBankAccount(
                bankAccountNumber = accountNumber,
                client = client
            )
        }
    ) { Icon(imageVector = Icons.Default.Delete, contentDescription = "delete account number") }
}

private fun accountNumberFormatter(number: String?): String {
    if(number == null) return ""
    var numbers: String = number.filterNot { it.isWhitespace() }
    var countryCode: String? = if(number.length >= 2 && number[0].isLetter() && number[1].isLetter()) number.substring(0, 2) else ""
    numbers = if(numbers.length >=2 ) numbers.filter { it.isDigit() } else numbers
    countryCode = if(countryCode?.isBlank() ?: true && number.length >=2 ) "PL" else countryCode?.uppercase()
    val iban = countryCode + numbers

    return iban.chunked(4).joinToString(" ").trim()
}

@Composable
fun AccountForm(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val currentBankAccount: ClientBankAccount? = viewModel.state.collectAsState().value.bankAccount
    val client: Client? = viewModel.state.collectAsState().value.client
    if(currentBankAccount == null) viewModel.updateBankAccount()
    val formatted: String = accountNumberFormatter(currentBankAccount?.bankAccount ?: "")

    OutlinedCard(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        OutlinedTextField(
            value = TextFieldValue(text = formatted, selection = TextRange(formatted.length)),
            onValueChange = { viewModel.updateBankAccount(currentBankAccount?.copy(bankAccount = it.text.take(34))) },
            label = { Text("Numer konta:") },
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = { viewModel.toClientData(client?.id) }
        ) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to client") }

        if(bankAccountValidator(currentBankAccount?.bankAccount)) {
            Button(
                onClick = {
                    val iban = currentBankAccount?.bankAccount!!
                    viewModel.addBankAccount(iban, client)
                    if(client?.id != null) viewModel.updateClient(client.id)
                    viewModel.newEmptyBankAccount()
                }
            ) { Text("Dodaj") }
        }
    }
}

private fun bankAccountValidator(number: String?): Boolean {
    if(number == null) return false
    val number = number.filterNot { it.isWhitespace() }
    if(number.length >= 2 && !number[0].isLetter() && !number[1].isLetter()) return false
    return number.length == 28
}

//@Preview(showBackground = true, widthDp = 340 )
//@Composable
//fun PreviewBankAccountMenu() {
//    val client = Client(
//        id = 1,
//        clientPrivate = null,
//        clientCompany = null,
//        isActive = true,
//        bankAccounts = listOf(
//            "61 1090 1014 0000 0712 1981 2874",
//            "61109010140000071219812874",
//            "PL61109010140000071219812874"
//        )
//    )
//    val viewModel = ParkingAppViewModel()
//    viewModel.updateClient(client)
//    BankAccountMenu(viewModel)
//}