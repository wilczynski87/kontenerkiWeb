package com.kontenery.library.model

import com.kontenery.library.serializers.LocalDateSerializer
import com.kontenery.model.Client
import com.kontenery.model.Product
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Contract(
    val id: Long? = null,
    var client: Client? = null,
    var product: Product? = null,
    @Serializable(with = LocalDateSerializer::class)
    var startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    var endDate: LocalDate? = null,
    var netPrice: Double? = null,
    val vatRate: Double = 23.00,
    var needInvoice: Boolean? = null,
    var deposit: Deposit? = null,
) {
    fun toContractDTO(): ContractDto =
        ContractDto(
            id = id,
            client = client?.id,
            product = product?.id,
            startDate = startDate,
            endDate = endDate,
            netPrice = netPrice,
            vatRate = vatRate,
            needInvoice = needInvoice
        )
}

@Serializable
data class ContractDto(
    val id: Long? = null,
    var client: Long? = null,
    var product: Long? = null,
    @Serializable(with = LocalDateSerializer::class)
    var startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    var endDate: LocalDate? = null,
    var netPrice: Double? = null,
    val vatRate: Double = 23.00,
    var needInvoice: Boolean? = null,
    var deposit: Deposit? = null,
) {

}