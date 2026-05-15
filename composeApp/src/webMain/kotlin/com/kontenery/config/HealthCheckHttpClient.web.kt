package com.kontenery.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout

internal actual fun createHealthCheckHttpClient(): HttpClient =
    HttpClient(Js) {
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = 4_000
            requestTimeoutMillis = 5_000
            socketTimeoutMillis = 5_000
        }
    }
