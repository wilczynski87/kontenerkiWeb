package com.kontenery.controller

import com.kontenery.TokenManager
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.error.AuthError
import com.kontenery.model.auth.AuthResponse
import com.kontenery.model.auth.LoginRequest
import com.kontenery.model.auth.RefreshTokenRequest
import com.kontenery.model.auth.UserInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.io.IOException

//expect fun tokenManagerInit(): TokenManager

class ApiAuth(
    private val httpClient: HttpClient
) {
    private var refreshAttempts = 0
    private val MAX_REFRESH_ATTEMPTS = 2
    suspend fun login(email: String, password: String): Result<UserInfo> {
        return try {
            val response: AuthResponse = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()

            // Zapisz tokeny
            TokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken
            )
            refreshAttempts = 0

            Result.success(UserInfo(
                id = response.loginResponse.userId,
                email = email,
                role = response.loginResponse.role
            ))
        } catch (e: ClientRequestException) {
            // 4xx
            when (e.response.status.value) {
                401 -> Result.failure(AuthError.InvalidCredentials)
                403 -> Result.failure(AuthError.Unauthorized)
                else -> Result.failure(AuthError.Server)
            }

        } catch (e: ServerResponseException) {
            // 5xx
            Result.failure(AuthError.Server)

        } catch (e: IOException) {
            // brak sieci / timeout
            Result.failure(AuthError.Network)

        } catch (e: Exception) {
            Result.failure(AuthError.Unknown(e))
        }
    }

    suspend fun refresh() : Result<UserInfo> {
        return try {
            val refreshToken = TokenManager.getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                return Result.failure(AuthError.InvalidCredentials)
            }
            val response: AuthResponse = httpClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }.body()

            // Zapisz tokeny
            TokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken
            )
            refreshAttempts = 0

            Result.success(UserInfo(
                id = response.loginResponse.userId,
                email = "",
                role = response.loginResponse.role
            ))

        } catch (e: ClientRequestException) {
            // Jeśli refresh token jest nieprawidłowy/wygasł
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                TokenManager.clearTokens() // wymagaj ponownego logowania
                Result.failure(AuthError.InvalidCredentials)
            } else {
                Result.failure(AuthError.Server)
            }
        } catch (e: ServerResponseException) {
            Result.failure(AuthError.Server)
        } catch (e: IOException) {
            Result.failure(AuthError.Network)
        } catch (e: Exception) {
            Result.failure(AuthError.Unknown(e))
        }
    }

    suspend fun logout() = safeRequest {
        httpClient.post("$baseUrl/auth/logout")
    }

    private suspend fun attemptRefresh(): Boolean {
        if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {
            TokenManager.clearTokens()
            return false
        }

        refreshAttempts++
        return runCatching {
            refresh().isSuccess
        }.getOrDefault(false)
    }


//    suspend fun <T> safeRequest(block: suspend () -> T): T {
//        var retryCount = 0
//        val maxRetries = 1
//
//        while (retryCount <= maxRetries) {
//            try {
//                val response = block()
//
//                // Sprawdź status odpowiedzi
//                when (response.status) {
//                    HttpStatusCode.Unauthorized -> {
//                        if (retryCount == 0) {
//                            // Spróbuj odświeżyć token
//                            if (TokenManager.getRefreshToken() != null && attemptRefresh()) {
//                                retryCount++
//                                continue // spróbuj ponownie z nowym tokenem
//                            }
//                        }
//                        // Jeśli nie udało się odświeżyć lub brak refresh tokena
//                        TokenManager.clearTokens()
//                        throw UnauthorizedException()
//                    }
//                    else -> {
//                        if (!response.status.isSuccess()) {
//                            // Obsłuż inne błędy HTTP
//                            throw when (response.status) {
//                                HttpStatusCode.Forbidden -> AuthError.Unauthorized
//                                else -> RuntimeException("HTTP ${response.status}")
//                            }
//                        }
//                        return response.body()
//                    }
//                }
//            } catch (e: ClientRequestException) {
//                if (e.response.status == HttpStatusCode.Unauthorized) {
//                    if (retryCount == 0 && TokenManager.getRefreshToken() != null && attemptRefresh()) {
//                        retryCount++
//                        continue
//                    }
//                    TokenManager.clearTokens()
//                    throw UnauthorizedException()
//                }
//                throw e
//            } catch (e: ServerResponseException) {
//                throw AuthError.Server
//            } catch (e: IOException) {
//                throw AuthError.Network
//            }
//        }
//
//        // Jeśli doszliśmy tutaj, nie udało się odświeżyć
//        TokenManager.clearTokens()
//        throw UnauthorizedException()
//    }

    suspend fun <T> safeRequest(
        maxRetries: Int = 1,
        block: suspend () -> T
    ): T {
        var retryCount = 0
        val tokenManager = TokenManager

        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                val isUnauthorized = when (e) {
                    is ClientRequestException -> e.response.status == HttpStatusCode.Unauthorized
                    is UnauthorizedException -> true
                    else -> false
                }

                if (isUnauthorized && retryCount < maxRetries && tokenManager.getRefreshToken() != null) {
                    // Spróbuj odświeżyć token
                    runCatching {
                        // Tutaj wywołaj refresh - potrzebujesz dostępu do ApiAuth
                        // To podejście wymaga przekazania ApiAuth
                    }
                    retryCount++
                    continue
                }

                if (isUnauthorized) {
                    tokenManager.clearTokens()
                }
                throw e
            }
        }
    }

}

class UnauthorizedException : RuntimeException()
