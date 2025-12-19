package com.kontenery

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kontenery.controller.ApiClientsService
import com.kontenery.service.ParkingAppViewModel
import com.kontenery.ui.ClientTable
import com.kontenery.ui.PaymentButtons
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    val scope = rememberCoroutineScope()
    val viewModel = remember { ParkingAppViewModel(scope) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.dispose()
        }
    }

    MaterialTheme {
        ClientTable(viewModel)
//        PaymentButtons()
    }
}