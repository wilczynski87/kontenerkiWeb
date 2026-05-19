package com.kontenery.controller

import com.kontenery.auth.TokenManager
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.model.auth.RefreshTokenRequest
import com.kontenery.model.auth.TokenResponse
import com.kontenery.serializers.productSerializersModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
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

@OptIn(ExperimentalWasmJsInterop::class)
fun webCreateHttpClient(tokenManager: TokenManager): HttpClient {
    return HttpClient(Js) {

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

        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }

        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        expectSuccess = true

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenManager.getAccessToken()
                    val refresh = tokenManager.getRefreshToken()

                    if (access != null) {
                        BearerTokens(
                            accessToken = access,
                            refreshToken = refresh ?: ""
                        )
                    } else null
                }

                refreshTokens {
                    val oldRefreshToken = tokenManager.getRefreshToken()
                        ?: return@refreshTokens null

                    try {
                        val refreshClient = HttpClient(Js) {
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

                        println("✅ Token refresh successful")

                        tokenManager.setTokens(
                            response.accessToken,
                            response.refreshToken
                        )

                        BearerTokens(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken ?: ""
                        )
                    }  catch (e: Exception) {
                        println("❌ Token refresh failed: ${e.message}")
                        if (e is ClientRequestException) {
                            try {
                                val errorBody = e.response.bodyAsText()
                                println("Error body: $errorBody")
                            } catch (ex: Exception) {
                                println(ex)
                            }
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