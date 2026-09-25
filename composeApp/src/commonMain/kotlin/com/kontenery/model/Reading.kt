package com.kontenery.model


import com.kontenery.model.enums.UtilityType
import com.kontenery.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Reading(
    val id: Long? = null,
    val submeterId: Long? = null,
    val utilityType: UtilityType? = null,
    val reading: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null,
    val currentUnitPriceNet: Double? = null
)
