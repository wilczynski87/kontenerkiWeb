package com.kontenery
import com.kontenery.auth.SecureTokenStorage
import com.kontenery.auth.TokenManager
import com.kontenery.controller.webCreateHttpClient
import io.ktor.client.HttpClient
import kotlinx.browser.localStorage

actual fun logDebug(tag: String, message: String) = println("DEBUG/$tag: $message")

actual fun logError(tag: String, message: String) = println("ERROR/$tag: $message")

actual fun provideSecureTokenStorage(): SecureTokenStorage {
    return JsSecureTokenStorage()
}

actual fun createHttpClient(tokenManager: TokenManager): HttpClient {
    return webCreateHttpClient(tokenManager)
}