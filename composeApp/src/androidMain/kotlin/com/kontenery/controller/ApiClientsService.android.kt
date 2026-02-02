package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.TokenManager
import com.kontenery.model.auth.RefreshTokenRequest
import com.kontenery.model.auth.TokenResponse
import com.kontenery.serializers.productSerializersModule
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient {
    return HttpClient(Android) {

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    classDiscriminator = "type"
                    serializersModule = productSerializersModule
                }
            )
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        expectSuccess = false

        install(HttpRequestRetry) {
            maxRetries = 3
            retryOnExceptionIf { request, cause ->
                cause is Exception
            }
            delayMillis { retry -> retry * 1000L } // Exponential backoff
        }

        install(HttpTimeout) {
            socketTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 30000
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = TokenManager.getAccessToken()
                    val refresh = TokenManager.getRefreshToken()
                    println("refresh: $refresh")

                    if (access != null) {
                        BearerTokens(
                            accessToken = access,
                            refreshToken = refresh ?: ""
                        )
                    } else null
                }

                refreshTokens {
                    val oldRefreshToken = TokenManager.getRefreshToken()
                        ?: return@refreshTokens null

                    try {
                        // Tworzymy tymczasowego klienta bez autoryzacji dla refresh token
                        val refreshClient = HttpClient(Android) {
                            install(ContentNegotiation) {
                                json(Json {
                                    ignoreUnknownKeys = true
                                    isLenient = true
                                })
                            }
                            install(HttpTimeout) {
                                requestTimeoutMillis = 15000
                            }
                        }

                        val response: TokenResponse = refreshClient.post("$baseUrl/auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(oldRefreshToken))
                        }.body()

                        refreshClient.close()

                        println("✅ Token refresh successful")

                        TokenManager.setTokens(
                            response.accessToken,
                            response.refreshToken
                        )

                        BearerTokens(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken ?: ""
                        )
                    } catch (e: ClientRequestException) {
                        println("❌ Token refresh failed: ${e.response.status} - ${e.message}")
                        try {
                            val errorBody = e.response.bodyAsText()
                            println("Error body: $errorBody")
                        } catch (ex: Exception) {
                            // Ignoruj
                        }
                        TokenManager.clearTokens()
                        null
                    } catch (e: Exception) {
                        println("❌ Token refresh failed: ${e.message}")
                        e.printStackTrace()
                        TokenManager.clearTokens()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    !request.url.encodedPath.contains("/auth/login") &&
                            !request.url.encodedPath.contains("/auth/register") &&
                            !request.url.encodedPath.contains("/auth/refresh")
                }
            }
        }

        // Logowanie dla debugowania
        engine {
            connectTimeout = 10000
            socketTimeout = 30000
        }
    }
}

//// Opcjonalnie: konfiguracja dla starszych wersji Androida
//fun createHttpClientWithCustomEngine(): HttpClient {
//    return HttpClient(Android.create {
//        // Konfiguracja dla starszych urządzeń
//        connectTimeout = 10000
//        socketTimeout = 30000
//        connectionSpecs = listOf(
//            ConnectionSpec.CLEARTEXT,
//            ConnectionSpec.MODERN_TLS
//        )
//    }) {
//        // ... reszta konfiguracji jak wyżej
//    }
//}