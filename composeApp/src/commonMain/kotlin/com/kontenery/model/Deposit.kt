package com.kontenery.model


import kotlinx.serialization.Serializable
import com.kontenery.model.enums.DepositType

@Serializable
data class Deposit(
    val type: DepositType? = null,
    val note: String? = null,
    val amount: String? = null
)