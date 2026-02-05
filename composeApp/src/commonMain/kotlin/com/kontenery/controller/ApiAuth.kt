package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.error.AuthError
import com.kontenery.model.TokenManager
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
            val response: AuthResponse = httpClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(TokenManager.getRefreshToken() ?: ""))
            }.body()

            // Zapisz tokeny
            TokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken
            )

            Result.success(UserInfo(
                id = response.loginResponse.userId,
                email = "",
                role = response.loginResponse.role
            ))

        } catch (e: Exception) {
            Result.failure(AuthError.Unknown(e))
        }
    }

    suspend fun logout() = safeRequest {
        httpClient.post("$baseUrl/auth/logout")
    }


}

//class AuthManager(private val apiAuth: ApiAuth) {
//    private val mutex = kotlinx.coroutines.sync.Mutex()
//
//    suspend fun refreshIfNeeded(): Boolean = mutex.withLock {
//        runCatching {
//            apiAuth.refresh()
//        }.isSuccess
//    }
//}

class UnauthorizedException : RuntimeException()

suspend fun <T> safeRequest(block: suspend () -> T): T {
    return try {
        val response = block()
        // ręczne sprawdzenie statusu, jeśli block zwraca np. HttpResponse
        response
    } catch (e: Exception) {
        if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
            // refresh token
//            val refreshed = authManager.refreshIfNeeded()
//            if (!refreshed) throw UnauthorizedException()
            return block() // retry
        }
        throw e
    }
}