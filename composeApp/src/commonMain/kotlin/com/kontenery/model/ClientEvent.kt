package com.kontenery.model

import com.kontenery.library.model.Address

sealed interface ClientEvent {

    // =========================
    // 🔹 PERSONAL DATA
    // =========================
    sealed interface Personal : ClientEvent {
        data class FirstNameChanged(val value: String) : Personal
        data class LastNameChanged(val value: String) : Personal
        data class PeselChanged(val value: String) : Personal
        data class PassportChanged(val value: String) : Personal
        data class PhoneChanged(val value: String) : Personal
        data class EmailChanged(val value: String) : Personal
        data class SalutationChanged(val value: String) : Personal
    }

    // =========================
    // 🔹 COMPANY DATA
    // =========================
    sealed interface Company : ClientEvent {
        data class NameChanged(val value: String) : Company
        data class NipChanged(val value: String) : Company
        data class KrsChanged(val value: String) : Company
        data class PhoneChanged(val value: String) : Company
        data class EmailChanged(val value: String) : Company
        data object ToggleInvoice : Company
    }

    // =========================
    // 🔹 ADDRESS
    // =========================
    sealed interface AddressEvent : ClientEvent {

        // personal address
        data class PersonalAddressChanged(val address: Address) : AddressEvent

        // company address
        data class CompanyAddressChanged(val address: Address) : AddressEvent
    }

    // =========================
    // 🔹 BANK ACCOUNTS
    // =========================
    sealed interface Bank : ClientEvent {
        data class Add(val account: String) : Bank
        data class Remove(val account: String) : Bank
        data class Update(val index: Int, val value: String) : Bank
    }

    // =========================
    // 🔹 CLIENT META
    // =========================
    data class InvoiceTitleChanged(val value: String) : ClientEvent

    data object ToggleActive : ClientEvent

    // =========================
    // 🔹 ACTIONS
    // =========================
    data object Save : ClientEvent
    data object Update : ClientEvent
}