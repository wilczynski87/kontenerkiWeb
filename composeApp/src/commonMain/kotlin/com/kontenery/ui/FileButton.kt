package com.kontenery.ui

import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.kontenery.FilePickerButton
import com.kontenery.PickFileFunc
import com.kontenery.controller.ApiClientsService
import com.kontenery.data.BankTransaction
import com.kontenery.data.CSVType
import com.kontenery.data.MessageRequest
import com.kontenery.data.parseBankTransactions
import kotlinx.coroutines.launch

@Composable
fun MyFilePickerButton(
    pickFile: PickFileFunc,
    csvType: CSVType? = null,
) {
    var lastName by remember { mutableStateOf<String?>(null) }
    var transactions by remember { mutableStateOf<List<BankTransaction>>(emptyList()) }

    val scope = rememberCoroutineScope()

    FilePickerButton(
        pickFile = pickFile,
        onPicked = { file ->
            scope.launch {
                lastName = file.name

                file.let {
                    val csvText = it.bytes.decodeToString()
                    ApiClientsService.csvPayments.sendCSVMessage(
                        MessageRequest(csvText),
                        csvType ?: CSVType.PEKAOSABUSSINESS,
                        )
                    transactions = parseBankTransactions(csvText)
                }
            }
        },
        buttonText = "Wybierz plik (KMP) dla: ${csvType?.endpoint}",
        fileType = csvType ?: CSVType.PEKAOSABUSSINESS
    )

    lastName?.let { Text("Ostatnio wybrany: $it") }
    if (transactions.isNotEmpty()) {
        Text("Załadowano ${transactions.size} transakcji")
        transactions.forEach { tx ->
            Text("${tx.bookingDate} - ${tx.counterparty} - ${tx.amount} ${tx.currency}")
        }
    }
}

