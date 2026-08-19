package com.kontenery.util

class BankAccount {
    companion object {

        fun toPolishIbanFormatted(countryCode: String = "PL", rawNrb: String): String {
            val digits = rawNrb.filter { it.isDigit() }
            val prefix = if (rawNrb.length >= 2 && rawNrb[0].isLetter() && rawNrb[1].isLetter())
                rawNrb.substring(0, 2).uppercase() else countryCode
            return (prefix + digits).chunked(4).joinToString(" ").trim()
        }
    }
}
