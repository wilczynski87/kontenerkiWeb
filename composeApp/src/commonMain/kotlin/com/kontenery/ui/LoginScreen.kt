package com.kontenery.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kontenery.service.ParkingAppViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: ParkingAppViewModel) {
    val state by viewModel.state.collectAsState()

    if (state.isLoggedIn) {
        Text("Zalogowany")
    } else {
        Button(onClick = {
//            scope.launch {
//                viewModel.login(email, password)
//            }
        }) {
            Text("Zaloguj")
        }
    }
}
