package com.kontenery

import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import javax.imageio.ImageIO

fun main() {
    System.setProperty("kotlinx.coroutines.fast.service.loader", "false")
    val icon = ImageIO.read(
        checkNotNull(Thread.currentThread().contextClassLoader.getResource("icon.png")),
    ).toPainter()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Kontenerki",
            icon = icon,
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
        ) {
            App()
        }
    }
}
