package com.kontenery

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kontenery.ui.ParkingApp
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.ui.AuthGate
import com.kontenery.ui.LoginScreen
import com.kontenery.ui.rememberWindowWidthSizeClass

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { ParkingAppViewModel(scope) }
    val widthClass = rememberWindowWidthSizeClass()

    when (widthClass) {
        WindowWidthSizeClass.Compact -> { /* UI na telefon / małe okno */ }
        WindowWidthSizeClass.Medium -> { /* UI tablet / normal */ }
        WindowWidthSizeClass.Expanded -> { /* duże ekrany */ }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.dispose()
        }
    }

    MaterialTheme {
        AuthGate(
            viewModel = viewModel,
            loginScreen = { LoginScreen(viewModel) }
        ) {
            ParkingApp(widthClass, viewModel)
        }
    }
}