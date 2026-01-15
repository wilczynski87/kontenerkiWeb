package com.kontenery.model.enums


enum class SellerAccount(val accountNumber: String) {
    BUSSINESS("PL50 1950 0001 2006 0023 6241 0001"),
    PRIVATE("PL11 2490 1044 0000 4200 8845 2192");

    companion object {
        fun fromAccountNumber(value: String): SellerAccount? {
            return entries.firstOrNull { it1 ->
                it1.accountNumber
                    .trim()
                    .filterNot { it.isWhitespace() } == value.trim().filterNot { it.isWhitespace() } }
        }
    }
}
