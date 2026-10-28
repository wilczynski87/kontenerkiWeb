package com.kontenery

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    System.setProperty("kotlinx.coroutines.fast.service.loader", "false")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Kontenerki",
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
        ) {
            App()
        }
    }
}
