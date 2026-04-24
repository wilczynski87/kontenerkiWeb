package com.kontenery.model.invoice

import com.kontenery.model.enums.InvoiceType
import com.kontenery.model.enums.UtilityType

data class InvoiceFeature(
    val invoiceType: InvoiceType,
    val utilityType: UtilityType? = null
)
