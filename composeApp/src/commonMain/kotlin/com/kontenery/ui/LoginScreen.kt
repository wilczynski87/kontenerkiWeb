package com.kontenery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.config.apiDeviceLabel
import com.kontenery.service.ParkingAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: ParkingAppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsState()
    val serverHealthStatus = state.serverHealthStatus
    val serverHealthProbeUrl = state.serverHealthProbeUrl
    val serverHealthTriedUrls = state.serverHealthTriedUrls
    val serverHealthLastError = state.serverHealthLastError

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Urządzenie: ${apiDeviceLabel()}", modifier = Modifier.padding(bottom = 8.dp))
            when {
                serverHealthStatus == null && serverHealthProbeUrl != null -> {
                    Text("Sprawdzam: $serverHealthProbeUrl")
                    LoadingDotsText()
                }
                serverHealthStatus == null -> {
                    Text("Sprawdzam serwer…")
                    LoadingDotsText()
                }
                serverHealthStatus == "server online" -> {
                    Text("$baseUrl — $serverHealthStatus")
                }
                else -> {
                    Text("$baseUrl — $serverHealthStatus")
                    if (serverHealthTriedUrls.isNotEmpty()) {
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = "Sprawdzono:\n${serverHealthTriedUrls.joinToString("\n") { "• $it" }}",
                        )
                    }
                    serverHealthLastError?.let { err ->
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = "Ostatni błąd: $err",
                        )
                    }
                }
            }
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.login(email, password)
                }
            }) {
                Text("Login")
            }
            Button(onClick = { viewModel.serverHealthCheck() }) {
                Text("Sprawdź ponownie")
            }
        }
    }
}
