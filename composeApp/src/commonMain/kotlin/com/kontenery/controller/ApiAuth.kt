package com.kontenery.controller

import com.kontenery.auth.TokenManager
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.error.AuthError
import com.kontenery.logDebug
import com.kontenery.model.auth.AuthResponse
import com.kontenery.model.auth.LoginRequest
import com.kontenery.model.auth.UserInfo
import com.kontenery.provideSecureTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.io.IOException

class ApiAuth(
    private val tokenManager: TokenManager,
    private val httpClient: HttpClient
) {

    suspend fun login(email: String, password: String): Result<UserInfo> {
        logDebug("login", "inside ApiAuth.login($email, $password)")
        return try {
            // Tymczasowo wyłącz Auth plugin dla login request
            val response: AuthResponse = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
                // Oznacz, żeby nie dodawać tokena
                markAsNotRequiringAuth()
            }.body()
            logDebug("login", "response: $response")


            // Tokeny zostaną zapisane przez Auth plugin po otrzymaniu odpowiedzi
            // Ale możemy też zapisać ręcznie dla pewności
            tokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken
            )

            val tokenManagerInfo = tokenManager.getAccessToken()
            logDebug("login", "tokenManagerInfo: $tokenManagerInfo")


            Result.success(UserInfo(
                id = response.loginResponse.userId,
                email = email,
                role = response.loginResponse.role
            ))
        } catch (e: ClientRequestException) {
            when (e.response.status.value) {
                401 -> Result.failure(AuthError.InvalidCredentials)
                403 -> Result.failure(AuthError.Unauthorized)
                else -> Result.failure(AuthError.Server)
            }
        } catch (e: ServerResponseException) {
            Result.failure(AuthError.Server)
        } catch (e: IOException) {
            Result.failure(AuthError.Network)
        } catch (e: Exception) {
            Result.failure(AuthError.Unknown(e))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            httpClient.post("$baseUrl/auth/logout") {
                contentType(ContentType.Application.Json)
            }

            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            // Nawet jeśli logout na serwerze się nie udał, wyczyść tokeny lokalnie
            tokenManager.clearTokens()
            Result.failure(AuthError.Unknown(e))
        }
    }

    suspend fun verifyToken(): Result<Boolean> {
        return try {
            // Proste zapytanie weryfikujące token
            httpClient.get("$baseUrl/auth/verify") {
                // Auth plugin automatycznie doda token
            }
            Result.success(true)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                tokenManager.clearTokens()
                Result.success(false)
            } else {
                Result.failure(AuthError.Server)
            }
        } catch (e: Exception) {
            Result.failure(AuthError.Unknown(e))
        }
    }

    // Helper extension dla requestów bez autoryzacji
    fun HttpRequestBuilder.markAsNotRequiringAuth() {
        headers.append("X-No-Auth", "true")
    }
}

// Extension dla HttpClient do łatwego tworzenia requestów
suspend inline fun <reified T> HttpClient.authGet(url: String): T {
    return this.get(url) {
        // Auth plugin automatycznie doda token
    }.body()
}

suspend inline fun <reified T, reified R> HttpClient.authPost(url: String, body: R): T {
    return this.post(url) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }.body()
}

// commonMain
class UnauthorizedException : RuntimeException("Authentication required")