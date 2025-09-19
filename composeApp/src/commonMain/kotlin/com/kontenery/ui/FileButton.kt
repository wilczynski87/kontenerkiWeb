package com.kontenery.ui

import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.kontenery.FilePickerButton
import com.kontenery.PickFileFunc
import com.kontenery.data.BankTransaction
import com.kontenery.data.parseBankTransactions
import com.kontenery.service.MessageRequest
import com.kontenery.service.sendCSVMessage
import kotlinx.coroutines.launch


@Composable
fun MyFilePickerButton(pickFile: PickFileFunc) {
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
                    sendCSVMessage(MessageRequest(csvText))
                    transactions = parseBankTransactions(csvText)
                }
            }
        },
        buttonText = "Wybierz plik (KMP)"
    )

    lastName?.let { Text("Ostatnio wybrany: $it") }
    if (transactions.isNotEmpty()) {
        Text("Załadowano ${transactions.size} transakcji")
        // tu możesz np. wyświetlić pierwsze 5 transakcji:
        transactions.forEach { tx ->
            Text("${tx.bookingDate} - ${tx.counterparty} - ${tx.amount} ${tx.currency}")
        }
    }
}

