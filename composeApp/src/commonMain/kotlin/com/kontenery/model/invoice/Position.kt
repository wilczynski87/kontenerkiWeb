package com.kontenery.library.model.invoice

import com.kontenery.library.model.Contract
import com.kontenery.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val productName:String? = null,
    val unitPrice:String? = null,
    val quantity:String? = null,
    val price:String? = null,
    val vatRate:String? = "23",
    val vatAmount:String? = null,
    val priceWithVat:String? = null,
) {
    companion object {
        fun toPosition(contract: Contract): Position {
            fun vatCalculate(): Double {
                val netPrice: Double = contract.netPrice?.round2() ?: return 0.00
                val vatRate: Double = contract.vatRate.round2()
                val vatProcent = (vatRate / 100).round2()
                return (netPrice * vatProcent).round2()
            }

            fun getQuantity(): Double {
                return (if(contract.product is Product.Yard) {
                    (contract.product as Product.Yard).quantity ?: 1.00
                } else 1.00) as Double
            }

            fun unitPriceCalculate(): Double? {
                return if(contract.product is Product.Yard) {
                    ((contract.netPrice ?: (0.00 / getQuantity())))
                } else contract.netPrice?.round2()
            }

            return Position(
                productName = contract.product?.name ?: "Błąd w nazwie",
                unitPrice = (unitPriceCalculate() ?: "Błąd w cenie").toString(),
                quantity = getQuantity().toString(),
                price = contract.netPrice.toString(),
                vatRate = contract.vatRate.toString(),
                vatAmount = vatCalculate().toString(),
                priceWithVat = (contract.netPrice?.plus(vatCalculate()) ?: "Błąd w obliczeniach").toString()
            )
        }

        fun Double.round2(): Double =
            kotlin.math.round(this * 100) / 100
    }

}
