package com.kontenery.controller

import com.kontenery.auth.TokenManager
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.logDebug
import com.kontenery.model.auth.RefreshTokenRequest
import com.kontenery.model.auth.TokenResponse
import com.kontenery.serializers.productSerializersModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun desktopCreateHttpClient(tokenManager: TokenManager): HttpClient {
    return HttpClient(CIO) {
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

        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }

        expectSuccess = true

        install(HttpRequestRetry) {
            maxRetries = 3
            retryOnExceptionIf { _, cause ->
                cause is java.io.IOException || cause is kotlinx.coroutines.TimeoutCancellationException
            }
            delayMillis { retry -> retry * 1000L }
        }

        install(HttpTimeout) {
            socketTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 30000
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenManager.getAccessToken()
                    val refresh = tokenManager.getRefreshToken()
                    logDebug("tokeny", "loading tokens access=${access != null} refresh=${refresh != null}")
                    if (access != null) {
                        BearerTokens(
                            accessToken = access,
                            refreshToken = refresh ?: ""
                        )
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val oldRefreshToken = tokenManager.getRefreshToken()
                        ?: return@refreshTokens null

                    try {
                        val refreshClient = HttpClient(CIO) {
                            expectSuccess = true
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

                        tokenManager.setTokens(
                            response.accessToken,
                            response.refreshToken
                        )

                        BearerTokens(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken ?: ""
                        )
                    } catch (e: Exception) {
                        if (e is ClientRequestException) {
                            runCatching { e.response.bodyAsText() }
                        }
                        tokenManager.clearTokens()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    val isAuthEndpoint = request.url.encodedPath.contains("/auth/login") ||
                        request.url.encodedPath.contains("/auth/register") ||
                        request.url.encodedPath.contains("/auth/refresh")
                    !isAuthEndpoint
                }
            }
        }
    }
}
