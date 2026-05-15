package com.kontenery.controller

import com.kontenery.auth.TokenManager
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.error.AuthError
import com.kontenery.logDebug
import com.kontenery.model.auth.AuthResponse
import com.kontenery.model.auth.LoginCredentials
import com.kontenery.model.auth.LoginResponse
import com.kontenery.model.auth.UserInfo
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
    private val httpClient: HttpClient,
) {
    suspend fun login(credentials: LoginCredentials): Result<UserInfo> {
        val username = credentials.username.trim()
        logDebug("ApiAuth", "login user=$username")
        return try {
            val response: AuthResponse = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(credentials.toRequest())
                markAsNotRequiringAuth()
            }.body()

            tokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken,
            )

            Result.success(
                UserInfo(
                    id = response.loginResponse.userId,
                    email = username,
                    role = response.loginResponse.role,
                ),
            )
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

    suspend fun clearStoredTokens() {
        tokenManager.clearTokens()
    }

    suspend fun logout(): Result<Unit> {
        return try {
            httpClient.post("$baseUrl/auth/logout") {
                contentType(ContentType.Application.Json)
            }
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.failure(AuthError.Unknown(e))
        }
    }

    suspend fun verifySession(): Result<LoginResponse> {
        return try {
            val response = httpClient.get("$baseUrl/auth/verify") {
                contentType(ContentType.Application.Json)
            }.body<AuthResponse>()

            tokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken,
            )
            Result.success(response.loginResponse)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                tokenManager.clearTokens()
            }
            Result.failure(
                when (e.response.status.value) {
                    401 -> AuthError.InvalidCredentials
                    403 -> AuthError.Unauthorized
                    else -> AuthError.Server
                },
            )
        } catch (e: IOException) {
            Result.failure(AuthError.Network)
        } catch (e: Exception) {
            Result.failure(AuthError.Unknown(e))
        }
    }

    private fun HttpRequestBuilder.markAsNotRequiringAuth() {
        headers.append("X-No-Auth", "true")
    }
}

class UnauthorizedException : RuntimeException("Authentication required")
