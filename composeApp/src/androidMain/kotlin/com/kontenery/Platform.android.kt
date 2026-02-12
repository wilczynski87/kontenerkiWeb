package com.kontenery

import android.os.Build
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual class TokenManager actual constructor() {

    // Używamy Application Context przekazanego przez DI
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    private fun getPrefs(): SharedPreferences? {
        return context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    actual fun setTokens(access: String, refresh: String?) {
        getPrefs()?.edit()?.apply {
            putString("access_token", access)
            putString("refresh_token", refresh)
            apply()
        }
    }

    actual fun getAccessToken(): String? =
        getPrefs()?.getString("access_token", null)

    actual fun getRefreshToken(): String? =
        getPrefs()?.getString("refresh_token", null)

    actual fun clearTokens() {
        getPrefs()?.edit()?.apply {
            remove("access_token")
            remove("refresh_token")
            apply()
        }
    }

    actual fun isAuthenticated(): Boolean = getAccessToken() != null

    actual companion object {
        // Singleton instance
        actual val instance: TokenManager by lazy { TokenManager() }
    }
}

actual fun logDebug(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun logError(tag: String, message: String) {
    Log.e(tag, message)
}