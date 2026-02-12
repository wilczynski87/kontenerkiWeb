package com.kontenery

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun logDebug(tag: String, message: String)
expect fun logError(tag: String, message: String)

// commonMain
expect class TokenManager() {
    fun setTokens(access: String, refresh: String?)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    fun isAuthenticated(): Boolean

    companion object {
        // Możemy dodać singleton pattern jeśli potrzebujemy
        val instance: TokenManager
    }
}