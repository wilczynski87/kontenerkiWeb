package com.kontenery

import com.kontenery.auth.SecureTokenStorage
import com.kontenery.auth.TokenManager
import io.ktor.client.HttpClient

interface Platform {
    val name: String
}
expect fun getPlatform(): Platform

expect fun provideSecureTokenStorage(): SecureTokenStorage

expect fun createHttpClient(tokenManager: TokenManager): HttpClient

expect fun logDebug(tag: String, message: String)
expect fun logError(tag: String, message: String)
