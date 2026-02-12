package com.kontenery.auth

interface SecureTokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun saveRefreshToken(token: String?)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clear()
}


class TokenManager(
    private val storage: SecureTokenStorage
) {
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    suspend fun setTokens(access: String, refresh: String?) {
        cachedAccessToken = access
        cachedRefreshToken = refresh

        storage.saveAccessToken(access)
        if (refresh != null) {
            storage.saveRefreshToken(refresh)
        }
    }

    suspend fun getAccessToken(): String? {
        if (cachedAccessToken == null) {
            cachedAccessToken = storage.getAccessToken()
        }
        return cachedAccessToken
    }

    suspend fun getRefreshToken(): String? {
        if (cachedRefreshToken == null) {
            cachedRefreshToken = storage.getRefreshToken()
        }
        return cachedRefreshToken
    }

    suspend fun clearTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
        storage.clear()
    }

    suspend fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
}
