package com.kontenery.model.invoice

import kotlinx.serialization.Serializable

/** Odpowiedź kontenerkiApi: POST /ksef/invoices/send */
@Serializable
data class KsefSendInvoiceResponse(
    val sessionReferenceNumber: String,
    val invoiceReferenceNumber: String,
    val ksefNumber: String? = null,
    val invoiceNumber: String? = null,
)
