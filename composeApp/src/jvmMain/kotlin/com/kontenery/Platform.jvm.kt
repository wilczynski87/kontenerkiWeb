package com.kontenery

import com.kontenery.auth.SecureTokenStorage
import com.kontenery.auth.TokenManager
import com.kontenery.controller.desktopCreateHttpClient
import io.ktor.client.HttpClient

private class DesktopPlatform : Platform {
    override val name: String = "Desktop ${System.getProperty("os.name")}"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun logDebug(tag: String, message: String) {
    println("DEBUG/$tag: $message")
}

actual fun logError(tag: String, message: String) {
    System.err.println("ERROR/$tag: $message")
}

actual fun provideSecureTokenStorage(): SecureTokenStorage = DesktopSecureTokenStorage()

actual fun createHttpClient(tokenManager: TokenManager): HttpClient =
    desktopCreateHttpClient(tokenManager)
