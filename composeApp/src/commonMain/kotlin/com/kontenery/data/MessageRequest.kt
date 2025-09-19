package com.kontenery.data

import kotlinx.serialization.Serializable


@Serializable
data class MessageRequest(
    val message: String
)
