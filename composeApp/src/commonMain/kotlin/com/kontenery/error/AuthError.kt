package com.kontenery.error

sealed class AuthError(message: String) : Throwable(message) {
    object InvalidCredentials : AuthError("Invalid credentials")
    object Network : AuthError("Network error")
    object Unauthorized : AuthError("Unauthorized")
    object Server : AuthError("Server error")
    data class Unknown(override val cause: Throwable) : AuthError(cause.message ?: "Unknown error")
}