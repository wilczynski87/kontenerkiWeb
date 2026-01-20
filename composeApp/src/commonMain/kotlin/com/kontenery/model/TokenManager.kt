package com.kontenery.model

object TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun setTokens(access: String, refresh: String?) {
        accessToken = access
        refreshToken = refresh
        println("Tokens set: access=${access.take(20)}..., refresh=${refresh?.take(20)}...")
    }

    fun getAccessToken(): String? = accessToken

    fun getRefreshToken(): String? = refreshToken

    fun clearTokens() {
        accessToken = null
        refreshToken = null
        println("Tokens cleared")
    }

    fun isAuthenticated(): Boolean = accessToken != null
}