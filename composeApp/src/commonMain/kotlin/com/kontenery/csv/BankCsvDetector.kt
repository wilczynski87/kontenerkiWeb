package com.kontenery.csv

import com.kontenery.data.CSVType

/**
 * Rozpoznaje bank po nagłówku wyciągu CSV, zanim plik trafi na endpoint importu.
 * Nest: wiersz „Numer rachunku:”. Alior: wiersz od „Data transakcji”.
 * Pekao SA: daty dd.MM.yyyy rozdzielane średnikiem albo nagłówek z datą księgowania i datą waluty.
 */
object BankCsvDetector {
    private val pekaoDate = Regex("""\d{2}\.\d{2}\.\d{4}""")

    fun detect(csvText: String): CSVType? {
        val lines = csvText
            .removePrefix("\uFEFF")
            .lineSequence()
            .map { it.trim().trimStart('\uFEFF') }
            .filter { it.isNotEmpty() }
            .take(HEADER_LINE_LIMIT)
            .toList()

        if (lines.any { it.startsWith("Numer rachunku:") }) {
            return CSVType.NEST
        }
        if (lines.any { it.startsWith("Data transakcji") }) {
            return CSVType.ALIOR
        }
        if (lines.any(::isPekaoLine)) {
            return CSVType.PEKAOSABUSSINESS
        }
        return null
    }

    private fun isPekaoLine(line: String): Boolean {
        if (!line.contains(';')) return false
        val firstField = line.substringBefore(';').trim().trim('"')
        if (pekaoDate.matches(firstField)) return true
        val lower = line.lowercase()
        return lower.contains("data ksi") && lower.contains("data waluty")
    }

    private const val HEADER_LINE_LIMIT = 80
}
