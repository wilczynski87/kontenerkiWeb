package com.kontenery.model.invoice

import com.kontenery.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class KsefSendResult(
    val invoiceNumber: String? = null,
    val ksefNumber: String? = null,
    val status: String? = null,
    val message: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val sentAt: LocalDate? = null,
)
