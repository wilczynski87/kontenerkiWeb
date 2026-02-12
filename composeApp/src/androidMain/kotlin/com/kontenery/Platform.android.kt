package com.kontenery

import android.content.Context
import android.os.Build
import android.util.Log
import com.kontenery.auth.SecureTokenStorage
import com.kontenery.auth.TokenManager
import com.kontenery.controller.androidCreateHttpClient
import io.ktor.client.HttpClient

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}
actual fun getPlatform(): Platform = AndroidPlatform()

actual fun logDebug(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun logError(tag: String, message: String) {
    Log.e(tag, message)
}

lateinit var appContext: Context
actual fun provideSecureTokenStorage(): SecureTokenStorage {
    return AndroidSecureTokenStorage(appContext)
}

actual fun createHttpClient(tokenManager: TokenManager): HttpClient {

    return androidCreateHttpClient(tokenManager)
}