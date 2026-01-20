package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.model.TokenManager
import com.kontenery.model.auth.AuthResponse
import com.kontenery.model.auth.LoginRequest
import com.kontenery.model.auth.LoginResponse
import com.kontenery.model.auth.TokenResponse
import com.kontenery.model.auth.UserInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.sync.withLock

lateinit var authManager: AuthManager
class ApiAuth(
    private val httpClient: HttpClient
) {
    suspend fun login(email: String, password: String): Result<UserInfo> = safeRequest {
        try {
            val response: AuthResponse = httpClient.post("$BASE_URL/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()

            // Zapisz tokeny
            TokenManager.setTokens(
                response.tokenResponse.accessToken,
                response.tokenResponse.refreshToken
            )

//            Result.success(response.loginResponse ?: UserInfo(
//                id = 0,
//                email = email,
//                role = "USER"
//            ))
            Result.success(UserInfo(
                id = response.loginResponse.userId,
                email = email,
                role = response.loginResponse.role
            ))
        } catch (e: Exception) {
            println("Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun refresh() = safeRequest {
        httpClient.post("$BASE_URL/auth/refresh")
    }

    suspend fun logout() = safeRequest {
        httpClient.post("$BASE_URL/auth/logout")
    }


}

class AuthManager(private val apiAuth: ApiAuth) {
    private val mutex = kotlinx.coroutines.sync.Mutex()

    suspend fun refreshIfNeeded(): Boolean = mutex.withLock {
        runCatching {
            apiAuth.refresh()
        }.isSuccess
    }
}

class UnauthorizedException : RuntimeException()

suspend fun <T> safeRequest(block: suspend () -> T): T {
    return try {
        val response = block()
        // ręczne sprawdzenie statusu, jeśli block zwraca np. HttpResponse
        response
    } catch (e: Exception) {
        if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
            // refresh token
            val refreshed = authManager.refreshIfNeeded()
            if (!refreshed) throw UnauthorizedException()
            return block() // retry
        }
        throw e
    }
}