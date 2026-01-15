package com.kontenery.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val userId: String,
    val role: String
)

@Serializable
data class AuthUser(
    val id: String,
    val role: String
)
