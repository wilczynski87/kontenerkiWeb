package com.kontenery.util

class BankAccount {
    companion object {

        fun toPolishIbanFormatted(countryCode: String = "PL", rawNrb: String): String {
            // Usuń spacje i znaki niebędące cyframi
            val nrb = rawNrb.filter { it.isDigit() }

            if (nrb.length != 26) {
                throw IllegalArgumentException("NRB musi mieć dokładnie 26 cyfr.")
            }

            // Oblicz sumę kontrolną IBAN
            val checksum = calculateIbanChecksum(countryCode, nrb)

            // Zbuduj pełny IBAN
            val iban = "PL$checksum$nrb"

            // Sformatuj: odstępy co 4 znaki
            return iban.chunked(4).joinToString(" ")
        }

        private fun calculateIbanChecksum(countryCode: String, nrb: String): String {
            // Przesunięcie: numer konta + kod kraju + 00
            val rearranged = nrb + countryCode + "00"

            // Zamiana liter na liczby (A = 10, ..., Z = 35)
            val numeric = rearranged.map {
                when (it) {
                    in '0'..'9' -> it - '0'
                    in 'A'..'Z' -> it.code - 'A'.code + 10
                    else -> error("Nieprawidłowy znak w numerze IBAN")
                }
            }

            // MOD 97
            val mod = mod97(numeric)
            val checkDigits = 98 - mod

            return checkDigits.toString().padStart(2, '0')
        }

        private fun mod97(digits: List<Int>): Int {
            var mod = 0
            for (digit in digits) {
                mod = (mod * 10 + digit) % 97
            }
            return mod
        }
    }
}
