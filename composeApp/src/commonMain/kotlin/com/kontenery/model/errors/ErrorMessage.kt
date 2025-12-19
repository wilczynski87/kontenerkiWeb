package com.kontenery.library.utils.errors

import kotlinx.serialization.Serializable

@Serializable
sealed interface ErrorMessage {
    val title: String?
    val message: String?
}