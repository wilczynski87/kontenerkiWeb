package com.kontenery.model.payment

import com.kontenery.model.PaymentDto
import com.kontenery.model.errors.PaymentError
import kotlinx.serialization.Serializable

@Serializable
data class PaymentsRecogniseList(
    val newPayments: List<PaymentDto>? = emptyList(),
    val oldPayments: List<PaymentDto>? = emptyList(),
    val unrecognizedPayments: List<PaymentDto>? = emptyList(),
    val errors: List<PaymentError>? = emptyList(),
)
