package com.kontenery

import com.kontenery.auth.SecureTokenStorage
import kotlinx.browser.localStorage

class JsSecureTokenStorage : SecureTokenStorage {

    override suspend fun saveAccessToken(token: String) {
        localStorage.setItem("access_token", token)
    }

    override suspend fun saveRefreshToken(token: String?) {
        if (token != null) {
            localStorage.setItem("refresh_token", token)
        } else {
            localStorage.removeItem("refresh_token")
        }
    }

    override suspend fun getAccessToken(): String? {
        return localStorage.getItem("access_token")
    }

    override suspend fun getRefreshToken(): String? {
        return localStorage.getItem("refresh_token")
    }

    override suspend fun clear() {
        localStorage.removeItem("access_token")
        localStorage.removeItem("refresh_token")
    }
}
