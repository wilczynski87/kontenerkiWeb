package com.kontenery.model.enums

enum class DepositType(
    val displayName: String
) {
    CASH("Cash"),
    BILL_OF_EXCHANGE("Bill of exchange"),
    INSURANCE("Insurance"),
    NONE("None");
}