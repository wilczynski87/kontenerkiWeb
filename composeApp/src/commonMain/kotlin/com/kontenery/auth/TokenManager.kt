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
            val token = storage.getAccessToken()
            cachedAccessToken = if (token.isNullOrBlank()) null else token
        }
        return cachedAccessToken
    }

    suspend fun getRefreshToken(): String? {
        if (cachedRefreshToken == null) {
            val token = storage.getRefreshToken()
            cachedRefreshToken = if (token.isNullOrBlank()) null else token
        }
        return cachedRefreshToken
    }

    /** Po błędzie odszyfrowania (np. InvalidKeyException) — wymuś ponowne logowanie. */
    suspend fun invalidateCachedTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
        storage.clear()
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
