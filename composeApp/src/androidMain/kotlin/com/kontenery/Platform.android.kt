package com.kontenery

import android.os.Build
import android.content.Context

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

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
    private const val PREFS_NAME = "auth_prefs"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    // W Androidzie potrzebujemy Context
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    private fun getPrefs() = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun setTokens(access: String, refresh: String?) {
        getPrefs()?.edit()?.apply {
            putString(ACCESS_TOKEN_KEY, access)
            putString(REFRESH_TOKEN_KEY, refresh)
            apply()
        }
    }

    actual fun getAccessToken(): String? =
        getPrefs()?.getString(ACCESS_TOKEN_KEY, null)

    actual fun getRefreshToken(): String? =
        getPrefs()?.getString(REFRESH_TOKEN_KEY, null)

    actual fun clearTokens() {
        getPrefs()?.edit()?.apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
    }

    actual fun isAuthenticated(): Boolean = getAccessToken() != null
}