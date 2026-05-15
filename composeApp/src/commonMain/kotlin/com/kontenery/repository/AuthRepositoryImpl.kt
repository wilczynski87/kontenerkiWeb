package com.kontenery.repository

import com.kontenery.controller.ApiClientsService
import com.kontenery.controller.HealthCheckResult
import com.kontenery.model.auth.LoginCredentials
import com.kontenery.model.auth.LoginResponse
import com.kontenery.model.auth.UserInfo

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(credentials: LoginCredentials): Result<UserInfo> =
        ApiClientsService.auth.login(credentials)

    override suspend fun verifySession(): Result<LoginResponse> =
        ApiClientsService.auth.verifySession()

    override suspend fun logout(): Result<Unit> =
        ApiClientsService.auth.logout()

    override suspend fun clearStoredTokens() {
        ApiClientsService.auth.clearStoredTokens()
    }

    override suspend fun checkServerHealth(onProbe: suspend (String) -> Unit): HealthCheckResult =
        ApiClientsService.healthCheck.healthCheck(onProbe)
}
