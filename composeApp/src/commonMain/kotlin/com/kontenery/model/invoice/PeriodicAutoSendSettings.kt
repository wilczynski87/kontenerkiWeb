package com.kontenery.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class PeriodicAutoSendSettings(
    val enabled: Boolean,
)
