package com.kontenery.auth

data class LoginValidationResult(
    val usernameError: String? = null,
    val passwordError: String? = null,
) {
    val isValid: Boolean = usernameError == null && passwordError == null
}

object LoginValidator {
    private const val MIN_PASSWORD_LENGTH = 1

    fun validate(username: String, password: String): LoginValidationResult {
        val trimmedUsername = username.trim()
        return LoginValidationResult(
            usernameError = when {
                trimmedUsername.isEmpty() -> "Podaj login"
                trimmedUsername.length < 2 -> "Login jest za krótki"
                else -> null
            },
            passwordError = when {
                password.isEmpty() -> "Podaj hasło"
                password.length < MIN_PASSWORD_LENGTH -> "Hasło jest za krótkie"
                else -> null
            },
        )
    }
}
