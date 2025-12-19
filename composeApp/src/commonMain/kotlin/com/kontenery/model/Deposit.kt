package com.kontenery.library.model


import kotlinx.serialization.Serializable
import com.kontenery.library.utils.DepositType

@Serializable
data class Deposit(
    val type: DepositType? = null,
    val note: String? = null,
    val amount: String? = null
)