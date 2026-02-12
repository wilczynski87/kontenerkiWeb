package com.kontenery
import kotlinx.browser.localStorage

actual class TokenManager actual constructor() {

    actual fun setTokens(access: String, refresh: String?) {
        localStorage.setItem("access_token", access)
        if (refresh != null) {
            localStorage.setItem("refresh_token", refresh)
        } else {
            localStorage.removeItem("refresh_token")
        }
    }

    actual fun getAccessToken(): String? =
        localStorage.getItem("access_token")

    actual fun getRefreshToken(): String? =
        localStorage.getItem("refresh_token")

    actual fun clearTokens() {
        localStorage.removeItem("access_token")
        localStorage.removeItem("refresh_token")
    }

    actual fun isAuthenticated(): Boolean = getAccessToken() != null

    // Dodatkowe metody specyficzne dla Wasm
    fun hasTokens(): Boolean = getAccessToken() != null || getRefreshToken() != null

    fun clearAllAuthData() {
        clearTokens()
        localStorage.removeItem("user_email")
        localStorage.removeItem("user_info")
    }

    actual companion object {
        actual val instance: TokenManager by lazy { TokenManager() }
    }
}

actual fun logDebug(tag: String, message: String) = println("DEBUG/$tag: $message")

actual fun logError(tag: String, message: String) = println("ERROR/$tag: $message")