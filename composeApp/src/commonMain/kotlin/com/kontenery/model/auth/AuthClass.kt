package com.kontenery.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserCredentials(
    val userName: String? = null,
    val password: String? = null,
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    val role: String,
    val name: String? = null
)

@Serializable
data class LoginResponse(
    val userId: String,
    val role: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresTn: Int? = null,
    val tokenType: String = "Bearer"
)

@Serializable
data class AuthResponse(
    val loginResponse: LoginResponse,
    val tokenResponse: TokenResponse
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)