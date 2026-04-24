package com.kontenery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.crypto.tink.aead.AeadConfig
import com.kontenery.auth.TokenManager

class MainActivity : ComponentActivity() {
    lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AeadConfig.register()
        appContext = applicationContext

        setContent {
            App()
        }
    }
}