package com.kontenery

interface Platform {
    val name: String
}

expect object TokenManager {
    fun setTokens(access: String, refresh: String?)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    fun isAuthenticated(): Boolean
}

expect fun getPlatform(): Platform