package com.kontenery.ui.login

import com.kontenery.auth.AuthDefaults

data class LoginUiState(
    val username: String = AuthDefaults.DEV_USERNAME,
    val password: String = AuthDefaults.DEV_PASSWORD,
    val isPasswordVisible: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val serverConnectivity: ServerConnectivity = ServerConnectivity.Idle,
)

sealed interface ServerConnectivity {
    data object Idle : ServerConnectivity
    data class Checking(val probeUrl: String?) : ServerConnectivity
    data class Online(val baseUrl: String) : ServerConnectivity
    data class Offline(
        val displayUrl: String,
        val triedUrls: List<String> = emptyList(),
        val lastError: String? = null,
    ) : ServerConnectivity
}
