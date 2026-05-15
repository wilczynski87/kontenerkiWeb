package com.kontenery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kontenery.service.ParkingAppViewModel

@Composable
fun AuthGate(
    viewModel: ParkingAppViewModel,
    loginScreen: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val authState = state.authState

    when {
        authState.loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        authState.isAuthenticated -> content()
        else -> loginScreen()
    }
}
