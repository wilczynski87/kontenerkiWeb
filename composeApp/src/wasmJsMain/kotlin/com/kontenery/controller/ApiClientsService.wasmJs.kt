package com.kontenery.controller

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient {
    return HttpClient(Js) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                }
            )
        }

//        install(Logging) {
//            level = LogLevel.ALL
//            logger = object : Logger {
//                override fun log(message: String) {
//                    println("HTTP: $message")
//                }
//            }
//        }
    }
}