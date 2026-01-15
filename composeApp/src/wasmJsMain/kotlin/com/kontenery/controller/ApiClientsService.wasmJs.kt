package com.kontenery.controller

import com.kontenery.serializers.productSerializersModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.HttpResponseValidator



actual fun createHttpClient(): HttpClient {
    return HttpClient(Js) {

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    classDiscriminator = "type"
                    serializersModule = productSerializersModule
                }
            )
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }

}