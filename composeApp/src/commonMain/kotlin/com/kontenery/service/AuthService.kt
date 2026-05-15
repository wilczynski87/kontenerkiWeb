package com.kontenery.service

import com.kontenery.auth.LoginValidator
import com.kontenery.auth.LoginValidationResult
import com.kontenery.error.AuthError
import com.kontenery.error.toAuthUserMessage
import com.kontenery.logDebug
import com.kontenery.logError
import com.kontenery.model.auth.LoginCredentials
import com.kontenery.model.auth.LoginResponse
import com.kontenery.model.auth.UserInfo
import com.kontenery.repository.AuthRepository
import com.kontenery.repository.AuthRepositoryImpl
import com.kontenery.ui.login.ServerConnectivity

class AuthService(
    private val repository: AuthRepository = AuthRepositoryImpl(),
) {
    fun validateForm(username: String, password: String): LoginValidationResult =
        LoginValidator.validate(username, password)

    suspend fun checkServerConnectivity(
        onProbe: suspend (String) -> Unit,
    ): ServerConnectivity {
        val result = repository.checkServerHealth(onProbe)
        return when {
            result.activeBaseUrl != null -> ServerConnectivity.Online(result.activeBaseUrl)
            else -> ServerConnectivity.Offline(
                displayUrl = result.triedUrls.lastOrNull().orEmpty(),
                triedUrls = result.triedUrls,
                lastError = result.lastError,
            )
        }
    }

    suspend fun login(credentials: LoginCredentials): Result<UserInfo> {
        logDebug("AuthService", "login user=${credentials.username.trim()}")
        return repository.login(credentials)
    }

    suspend fun restoreSession(): Result<LoginResponse> {
        logDebug("AuthService", "restoreSession")
        return repository.verifySession()
    }

    suspend fun clearSession() {
        repository.clearStoredTokens()
    }

    suspend fun logout(): Result<Unit> = repository.logout()

    fun mapSessionFailure(error: Throwable): String {
        if (isSecureStorageFailure(error)) {
            return "Sesja wygasła — zaloguj się ponownie"
        }
        return when (error) {
            is AuthError -> error.toAuthUserMessage()
            else -> "Sesja wygasła — zaloguj się ponownie"
        }
    }

    fun mapLoginFailure(error: Throwable): String = error.toAuthUserMessage()
}

private fun isSecureStorageFailure(error: Throwable): Boolean {
    var current: Throwable? = error
    while (current != null) {
        val name = current::class.simpleName.orEmpty()
        if (name.contains("InvalidKey", ignoreCase = true)
            || name.contains("GeneralSecurity", ignoreCase = true)
            || name.contains("KeyStore", ignoreCase = true)
        ) {
            return true
        }
        if (current.message?.contains("invalid key", ignoreCase = true) == true) {
            return true
        }
        current = current.cause
    }
    return false
}
