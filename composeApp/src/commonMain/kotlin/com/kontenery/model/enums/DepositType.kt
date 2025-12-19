package com.kontenery.library.utils

enum class DepositType(
    val displayName: String
) {
    CASH("Cash"),
    BILL_OF_EXCHANGE("Bill of exchange"),
    INSURANCE("Insurance"),
    NONE("None");
}