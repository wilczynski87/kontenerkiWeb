package com.kontenery
import kotlinx.browser.localStorage

//actual object TokenManager {
//    actual fun setTokens(access: String, refresh: String?) {
//    }
//
//    actual fun getAccessToken(): String? {
//        TODO("Not yet implemented")
//    }
//
//    actual fun getRefreshToken(): String? {
//        TODO("Not yet implemented")
//    }
//
//    actual fun clearTokens() {
//    }
//
//    actual fun isAuthenticated(): Boolean {
//        TODO("Not yet implemented")
//    }
//}

actual object TokenManager {
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    actual fun setTokens(access: String, refresh: String?) {
        localStorage.setItem(ACCESS_TOKEN_KEY, access)
        if (refresh != null) {
            localStorage.setItem(REFRESH_TOKEN_KEY, refresh)
        } else {
            localStorage.removeItem(REFRESH_TOKEN_KEY)
        }
    }

    actual fun getAccessToken(): String? =
        localStorage.getItem(ACCESS_TOKEN_KEY)

    actual fun getRefreshToken(): String? =
        localStorage.getItem(REFRESH_TOKEN_KEY)

    actual fun clearTokens() {
        localStorage.removeItem(ACCESS_TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
    }

    actual fun isAuthenticated(): Boolean = getAccessToken() != null
}