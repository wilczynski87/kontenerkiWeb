package com.example.parkingandroidview.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.library.model.Deposit
import com.kontenery.library.utils.DepositType
import com.kontenery.service.ParkingAppViewModel

@Composable
fun DepositOptionSelector(
    viewModel: ParkingAppViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val deposit = state.contract?.deposit ?: Deposit()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Forma depozytu",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DepositType.entries.forEach { type ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.depositChange(deposit.copy(type = type))
                        println("deosit type: ${deposit}")
                    }
                    .padding(vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = deposit.type == type,
                        onClick = {
                            viewModel.depositChange(deposit.copy(type = type))
                            println("deosit value: ${deposit}")
                        }
                    )
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // GOTÓWKA
                if (type == DepositType.CASH && deposit.type == DepositType.CASH) {
                    OutlinedTextField(
                        value = deposit.amount ?: "",
                        onValueChange = { viewModel.depositChange(deposit.copy(amount = it)) },
                        label = { Text("Kwota") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, top = 4.dp)
                    )
                }

                // WEKSEL
                if (type == DepositType.BILL_OF_EXCHANGE && deposit.type == DepositType.BILL_OF_EXCHANGE) {
                    OutlinedTextField(
                        value = deposit.amount ?: "",
                        onValueChange = { viewModel.depositChange(deposit.copy(amount = it)) },
                        label = { Text("Kwota") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, top = 4.dp)
                    )
                }

                // UBEZPIECZENIE
                if (type == DepositType.INSURANCE && deposit.type == DepositType.INSURANCE) {
                    OutlinedTextField(
                        value = deposit.note ?: "",
                        onValueChange = { viewModel.depositChange(deposit.copy(note = it)) },
                        label = { Text("Numer oraz ubezpieczyciel") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, top = 4.dp)
                    )
                }

                // BRAK
                if (type == DepositType.INSURANCE && deposit.type == DepositType.INSURANCE) {
                    viewModel.depositChange(deposit.copy(note = null, amount = null))
                }
            }
        }
    }
}

//@Preview(showBackground = true, widthDp = 500 )
//@Composable
//fun DepozytPreview() {
//    val viewModel = ParkingAppViewModel()
//    val state = ParkingAppState().copy(contract = Contract().copy(deposit = Deposit(type = DepositType.INSURANCE)))
//    viewModel.setState(state)
//    DepositOptionSelector(
//        viewModel
//    )
//}