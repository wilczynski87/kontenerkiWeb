package com.kontenery.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val url: String = "http://localhost:8080/"

@Serializable
data class MessageRequest(
    val message: String
)

// jeden wspólny klient
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

// przykład POST
suspend fun sendCSVMessage(message: MessageRequest): MessageRequest {
    return httpClient.post("$url/csv/PeKaOSA") {
        contentType(ContentType.Application.Json)
        setBody(message)
    }.body()
}

// przykład GET
suspend fun fetchMessages(): List<MessageRequest> {
    return httpClient.post("$url/csv/PeKaOSA").body()
}
