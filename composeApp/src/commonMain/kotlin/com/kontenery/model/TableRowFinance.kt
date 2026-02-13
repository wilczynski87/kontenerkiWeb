package com.kontenery.model

import kotlinx.serialization.Serializable

@Serializable
data class TableRowFinance(
    val clientId: Long?,
    val name: String,
    val values: Map<String, List<PaymentForFinanceTable?>>, // month -> amount
    val isActive: Boolean
)
