package com.kontenery.data

import com.kontenery.model.auth.LoginResponse
import kotlinx.serialization.Serializable

@Serializable
data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: LoginResponse? = null,
    val loading: Boolean = false,
    val error: String? = null
)
