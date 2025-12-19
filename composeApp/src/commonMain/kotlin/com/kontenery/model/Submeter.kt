package com.kontenery.library.model

import com.kontenery.model.enums.UtilityType
import kotlinx.serialization.Serializable

@Serializable
data class Submeter(
    val id: Long? = null,
    val clientId: Long? = null,
    val location: String? = null,
    val utilityType: UtilityType? = null,
    val readings: List<Reading> = emptyList(),
    val number: String? = null,
)