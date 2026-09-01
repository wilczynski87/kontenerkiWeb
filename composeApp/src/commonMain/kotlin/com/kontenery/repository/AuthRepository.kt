package com.kontenery.repository

import com.kontenery.controller.HealthCheckResult
import com.kontenery.model.auth.LoginCredentials
import com.kontenery.model.auth.LoginResponse
import com.kontenery.model.auth.UserInfo

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): Result<UserInfo>
    suspend fun loginWithGoogle(idToken: String): Result<UserInfo>
    suspend fun verifySession(): Result<LoginResponse>
    suspend fun logout(): Result<Unit>
    suspend fun clearStoredTokens()
    suspend fun checkServerHealth(onProbe: suspend (String) -> Unit): HealthCheckResult
}
