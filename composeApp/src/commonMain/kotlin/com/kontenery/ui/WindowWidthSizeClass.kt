package com.kontenery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.kontenery.model.enums.WindowWidthSizeClass
import com.kontenery.service.calculateWidthSizeClass

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
    val density = LocalDensity.current

    val widthPx = windowInfo.containerSize.width.toFloat()

    val widthDp = with(density) { widthPx.toDp().value }

    return remember(widthDp) {
        calculateWidthSizeClass(widthDp)
    }
}
