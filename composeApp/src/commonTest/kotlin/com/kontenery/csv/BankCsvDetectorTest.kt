package com.kontenery.csv

import com.kontenery.data.CSVType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BankCsvDetectorTest {

    @Test
    fun detectsNestFromAccountHeader() {
        val csv = """
            Numer rachunku: 51187010452078108959440001
            Właściciel: KONTENERY
            Data księgowania,Data operacji,Rodzaj operacji,Kwota,Waluta,Dane kontrahenta,Numer rachunku kontrahenta,Tytuł operacji,Saldo po operacji
            15-04-2026,15-04-2026,Przelewy przychodzące,984,PLN,JAN|ULICA,72114020040000320278657853,18/4/2026,9419.4
        """.trimIndent()

        assertEquals(CSVType.NEST, BankCsvDetector.detect(csv))
    }

    @Test
    fun detectsNestEvenWhenHistoryIsEmpty() {
        val csv = """
            Numer rachunku: 51187010452078108959440001
            Liczba operacji: 0
        """.trimIndent()

        assertEquals(CSVType.NEST, BankCsvDetector.detect(csv))
    }

    @Test
    fun detectsAliorFromTransactionHeader() {
        val csv = """
            Historia operacji
            Data transakcji;Data księgowania;Kontrahent;Adres;Tytuł;Kwota;Pole;Pole;Waluta;Rachunek źródłowy;Rachunek docelowy
            15-04-2026;15-04-2026;JAN;ULICA;18/4/2026;984,00;x;x;PLN;72114020040000320278657853;51187010452078108959440001
        """.trimIndent()

        assertEquals(CSVType.ALIOR, BankCsvDetector.detect(csv))
    }

    @Test
    fun detectsPekaoFromDottedDate() {
        val csv = """
            Data księgowania;Data waluty;Nadawca;Adres;Rachunek źródłowy;Rachunek docelowy;Tytułem;Kwota;Waluta;Numer referencyjny;Typ operacji
            01.04.2026;01.04.2026;JAN KOWALSKI;UL. TEST;123;456;FV 1/4/2026;1 200,00;PLN;ref;Przelew
        """.trimIndent()

        assertEquals(CSVType.PEKAOSABUSSINESS, BankCsvDetector.detect(csv))
    }

    @Test
    fun detectsPekaoFromHeaderOnly() {
        val csv = "Data księgowania;Data waluty;Nadawca / Odbiorca;Tytułem"

        assertEquals(CSVType.PEKAOSABUSSINESS, BankCsvDetector.detect(csv))
    }

    @Test
    fun returnsNullForUnknownFile() {
        assertNull(BankCsvDetector.detect("to nie jest wyciąg\ntylko notatka"))
    }

    @Test
    fun ignoresBomBeforeNestHeader() {
        val csv = "\uFEFFNumer rachunku: 51187010452078108959440001\n"

        assertEquals(CSVType.NEST, BankCsvDetector.detect(csv))
    }
}
