package com.kontenery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kontenery.service.ParkingAppViewModel
import io.ktor.websocket.Frame

@Composable
fun AuthGate(
    viewModel: ParkingAppViewModel,
    loginScreen: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val authState = state.authState


    when {
        authState.loading -> {
            // można dać spinner
            Frame.Text("Loading…")
        }
        authState.isAuthenticated -> {
            // użytkownik zalogowany → renderuj całą stronę
            content()
        }
        else -> {
            // użytkownik niezalogowany → renderuj ekran logowania
            loginScreen()
        }
    }
}