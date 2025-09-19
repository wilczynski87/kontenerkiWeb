package com.kontenery.data

import com.kontenery.pickFile
import kotlinx.datetime.LocalDate

//@Serialization
data class BankTransaction(
    val bookingDate: LocalDate,        // Data księgowania
    val valueDate: LocalDate,          // Data waluty
    val counterparty: String,          // Nadawca / Odbiorca
    val counterpartyAddress: String,   // Adres nadawcy / odbiorcy
    val sourceAccount: String,         // Rachunek źródłowy
    val targetAccount: String,         // Rachunek docelowy
    val title: String,                 // Tytułem
    val amount: Double,                // Kwota operacji
    val currency: String,              // Waluta
    val referenceNumber: String,       // Numer referencyjny
    val operationType: String          // Typ operacji
)

fun parseBankTransactions(csvContent: String): List<BankTransaction> {
    return csvContent
        .lineSequence()
        .drop(1)
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            val cols = line.split(';')
            if (cols.size < 11) return@mapNotNull null

            try {
                val parseDate: (String) -> LocalDate = { dateStr ->
                    val (d, m, y) = dateStr.trim().split('.')
                    LocalDate(y.toInt(), m.toInt(), d.toInt())
                }

                BankTransaction(
                    bookingDate = parseDate(cols[0]),
                    valueDate = parseDate(cols[1]),
                    counterparty = cols[2].trim(),
                    counterpartyAddress = cols[3].trim(),
                    sourceAccount = cols[4].trim().trim('\''),
                    targetAccount = cols[5].trim().trim('\''),
                    title = cols[6].trim(),
                    amount = cols[7].trim().replace("\\s+".toRegex(), "").replace(",", ".").toDouble(),
                    currency = cols[8].trim(),
                    referenceNumber = cols[9].trim().trim('\''),
                    operationType = cols[10].trim()
                )
            } catch (e: Exception) {
                println("Błąd parsowania wiersza: $line -> ${e.message}")
                null
            }
        }
        .toList()
}
