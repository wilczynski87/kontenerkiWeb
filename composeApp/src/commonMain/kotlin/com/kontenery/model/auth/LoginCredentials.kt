package com.kontenery.model.auth

/**
 * Dane logowania wysyłane do API (`POST /auth/login`).
 * Pole `email` w JSON odpowiada loginowi użytkownika (np. „ppp”).
 */
data class LoginCredentials(
    val username: String,
    val password: String,
) {
    fun toRequest(): LoginRequest = LoginRequest(
        email = username.trim(),
        password = password,
    )
}
