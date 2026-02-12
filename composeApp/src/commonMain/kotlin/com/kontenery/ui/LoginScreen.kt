package com.kontenery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kontenery.controller.ApiClientsService
import com.kontenery.model.auth.UserCredentials
import com.kontenery.service.ParkingAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: ParkingAppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            Button(onClick = {
                // wywołanie login w coroutine
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.login(email, password)
                }
            }) {
                Text("Login")
            }
        }
    }
}
