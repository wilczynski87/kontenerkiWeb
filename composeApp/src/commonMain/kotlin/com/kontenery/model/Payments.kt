package com.kontenery.library.model

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.serializers.LocalDateSerializer
import com.kontenery.library.utils.SellerAccount
import com.kontenery.model.Client
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: Long? = null,
    val amount: Double,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val fromClient: Client? = null,
    val method: String? = null,
    val toAccount: SellerAccount? = SellerAccount.BUSSINESS,
    val fromAccount: String? = null,
    val title: String? = null,
    val forInvoices: List<Invoice> = listOf(),
    val referenceNumber: String? = null

    ) {

    fun toDto(): PaymentDto {
        return PaymentDto(
            paymentId = this.id.toString(),
            amount = this.amount,
            date = this.date,
            fromClientId = this.fromClient?.id,
            method = this.method,
            toAccount = this.toAccount?.accountNumber,
            fromAccount = this.fromAccount,
            title = this.title,
            forInvoices = this.forInvoices.mapNotNull { it.invoiceNumber },
            referenceNumber = this.referenceNumber
        )
    }
}

@Serializable
data class PaymentDto(
    val paymentId: String? = null,
    val amount: Double? = null,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null,
    val fromClientId: Long? = null,
    val method: String? = null,
    val toAccount: String? = null,
    val fromAccount: String? = null,
    val title: String? = null,
    val forInvoices: List<String>? = null,
    val referenceNumber: String? = null,
) {
//    fun toPayment(): Payment {
//        return Payment(
//            id = this.paymentId?.toLongOrNull(),
//            amount = this.amount ?: throw NullPointerException("toPayment, lack of 'AMOUNT'"),
//            date = this.date ?: throw NullPointerException("toPayment, lack of 'DATE'"),
//            fromClientId = this.fromClient?.id,
//            method = this.method,
//            toAccount = this.toAccount?.accountNumber,
//            fromAccount = this.fromAccount,
//            title = this.title,
//            forInvoices = this.forInvoices.mapNotNull { it.invoiceNumber }
//        )
//    }
}

@Serializable
enum class PaymentMethod(val polishName: String) {
    TRANSFER("przelew"),
    BLIK("blik"),
    CASH("gotówka"),
    OTHER("inna");

    companion object {
        fun fromName(polishName: String): PaymentMethod? = PaymentMethod.entries.find {
            it.polishName == polishName
        }
    }
}