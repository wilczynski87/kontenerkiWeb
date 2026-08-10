package com.kontenery.ui.login

data class LoginUiState(
    val username: String = "",
    val password: String = "",
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
