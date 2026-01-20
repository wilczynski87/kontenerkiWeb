package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.model.TokenManager
import com.kontenery.model.auth.TokenResponse
import com.kontenery.serializers.productSerializersModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.engine.js.JsClientEngineConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials

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

                    if (access != null) {
                        BearerTokens(
                            accessToken = access,
                            refreshToken = refresh ?: ""
                        )
                    } else null
                }

//                refreshTokens {
//                    val oldRefreshToken = TokenManager.getRefreshToken()
//                        ?: return@refreshTokens null
//
//                    try {
//                        val response: TokenResponse = client.post("$BASE_URL/auth/refresh") {
//                            markAsRefreshTokenRequest()
//                            contentType(ContentType.Application.Json)
//                            setBody(RefreshRequest(oldRefreshToken))
//                        }.body()
//
//                        TokenManager.setTokens(
//                            response.access_token,
//                            response.refresh_token
//                        )
//
//                        BearerTokens(
//                            accessToken = response.access_token,
//                            refreshToken = response.refresh_token ?: ""
//                        )
//                    } catch (e: Exception) {
//                        println("Token refresh failed: ${e.message}")
//                        TokenManager.clearTokens()
//                        null
//                    }
//                }

                sendWithoutRequest { request ->
                    // Zawsze wysyłaj token (oprócz endpointów auth)
                    !request.url.encodedPath.contains("/auth/login") &&
                            !request.url.encodedPath.contains("/auth/register")
                }
            }
        }

    }
}