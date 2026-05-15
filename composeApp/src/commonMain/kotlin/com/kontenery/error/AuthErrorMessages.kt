package com.kontenery.error

fun AuthError.toUserMessage(): String = when (this) {
    AuthError.InvalidCredentials -> "Nieprawidłowy login lub hasło"
    AuthError.Network -> "Brak połączenia z serwerem"
    AuthError.Unauthorized -> "Brak uprawnień do konta"
    AuthError.Server -> "Błąd serwera — spróbuj ponownie później"
    is AuthError.Unknown -> cause?.message?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: "Wystąpił nieoczekiwany błąd"
}

fun Throwable.toAuthUserMessage(): String = when (this) {
    is AuthError -> toUserMessage()
    else -> message?.takeIf { it.isNotBlank() } ?: "Wystąpił nieoczekiwany błąd"
}
