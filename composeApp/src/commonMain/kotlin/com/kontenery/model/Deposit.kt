package com.kontenery.model


import com.kontenery.model.enums.DepositType
import kotlinx.serialization.Serializable

@Serializable
data class Deposit(
    val type: DepositType? = null,
    val note: String? = null,
    val amount: String? = null
)