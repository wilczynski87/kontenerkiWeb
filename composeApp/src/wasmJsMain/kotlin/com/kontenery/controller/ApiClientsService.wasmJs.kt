package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.model.TokenManager
import com.kontenery.model.auth.TokenResponse
import com.kontenery.serializers.productSerializersModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@OptIn(ExperimentalWasmJsInterop::class)
actual fun createHttpClient(): HttpClient {

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

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        expectSuccess = false

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
                        val response: TokenResponse = client.post("$BASE_URL/auth/refresh") {
                            markAsRefreshTokenRequest()
                            contentType(ContentType.Application.Json)
//                            header(HttpHeaders.Authorization, "Bearer $oldRefreshToken")
                            setBody(oldRefreshToken)
                        }.body()

                        println("✅ Token refresh successful")

                        TokenManager.setTokens(
                            response.accessToken,
                            response.refreshToken
                        )

                        BearerTokens(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken ?: ""
                        )
                    }  catch (e: ClientRequestException) {
                        println("❌ Token refresh failed: ${e.response.status} - ${e.message}")
                        // Spróbuj odczytać body błędu
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
                    // Zawsze wysyłaj token (oprócz endpointów auth)
                    !request.url.encodedPath.contains("/auth/login") &&
                    !request.url.encodedPath.contains("/auth/register")
                }
            }
        }

    }
}