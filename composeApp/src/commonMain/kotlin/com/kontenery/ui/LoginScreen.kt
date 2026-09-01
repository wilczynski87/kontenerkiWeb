package com.kontenery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.config.apiDeviceLabel
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.ui.login.ServerConnectivity

@Composable
fun LoginScreen(viewModel: ParkingAppViewModel) {
    val state by viewModel.state.collectAsState()
    val loginUi = state.loginUi
    val authState = state.authState

    val isServerOnline = loginUi.serverConnectivity is ServerConnectivity.Online
    val isCheckingServer = loginUi.serverConnectivity is ServerConnectivity.Checking
        || loginUi.serverConnectivity is ServerConnectivity.Idle
    val canSubmit = isServerOnline && !authState.loading && !isCheckingServer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Kontenery",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = apiDeviceLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        ServerStatusCard(connectivity = loginUi.serverConnectivity)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = loginUi.username,
            onValueChange = viewModel::onLoginUsernameChange,
            label = { Text("Login") },
            singleLine = true,
            isError = loginUi.usernameError != null,
            supportingText = loginUi.usernameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            enabled = !authState.loading,
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = loginUi.password,
            onValueChange = viewModel::onLoginPasswordChange,
            label = { Text("Hasło") },
            singleLine = true,
            isError = loginUi.passwordError != null,
            supportingText = loginUi.passwordError?.let { { Text(it) } },
            visualTransformation = if (loginUi.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (canSubmit) viewModel.submitLogin() },
            ),
            trailingIcon = {
                IconButton(onClick = viewModel::onLoginPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (loginUi.isPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (loginUi.isPasswordVisible) {
                            "Ukryj hasło"
                        } else {
                            "Pokaż hasło"
                        },
                    )
                }
            },
            enabled = !authState.loading,
        )

        authState.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = viewModel::submitLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit,
        ) {
            if (authState.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Zaloguj się")
            }
        }

        if (viewModel.isGoogleSignInAvailable()) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = viewModel::submitGoogleLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
            ) {
                Text("Zaloguj przez Google")
            }
        }

        TextButton(
            onClick = viewModel::checkServerConnectivity,
            enabled = !authState.loading && !isCheckingServer,
        ) {
            Text("Sprawdź połączenie z serwerem")
        }
    }
}

@Composable
private fun ServerStatusCard(connectivity: ServerConnectivity) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        when (connectivity) {
            ServerConnectivity.Idle,
            is ServerConnectivity.Checking -> {
                Text("Sprawdzam serwer…", style = MaterialTheme.typography.bodyMedium)
                connectivity.let { state ->
                    if (state is ServerConnectivity.Checking && state.probeUrl != null) {
                        Text(
                            text = state.probeUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                LoadingDotsText()
            }
            is ServerConnectivity.Online -> {
                Text(
                    text = "Serwer dostępny",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = connectivity.baseUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is ServerConnectivity.Offline -> {
                Text(
                    text = "Brak połączenia z serwerem",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (connectivity.triedUrls.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Sprawdzono:\n${connectivity.triedUrls.joinToString("\n") { "• $it" }}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                connectivity.lastError?.let { err ->
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Ostatni błąd: $err",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "Aktywny URL: $baseUrl",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
