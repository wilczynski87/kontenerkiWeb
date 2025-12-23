package com.kontenery.model.enums

enum class ClientFilter(val label: String) {
    ALL("Wszyscy"),
    //    ACTIVE("Aktywni"),
    INACTIVE("Nieaktywni"),
    OVERDUE("Zaległości"),
    OVERPAID("Nadpłaty"),
    INVOICE("Faktury"),
    BILL("Rachunki"),
    NOCONTRACT("Bez umów"),
}

enum class ContractFilter(val label: String) {
    ALL("Wszystkie"),

    NOT_SEND("Nie wysłane"),
}

enum class ProductFilter(val label: String) {
    ALL("Wszystkie"),
    ACTIVE("Aktywne"),
    TYPE("Typ"),
    SUBTYPE("Podtyp"),
}