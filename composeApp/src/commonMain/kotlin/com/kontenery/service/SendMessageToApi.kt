package com.kontenery.service

import com.kontenery.data.CSVType
import com.kontenery.data.MessageRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val url: String = "http://localhost:8100"

// jeden wspólny klient
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

// przykład POST
suspend fun sendCSVMessage(
    message: MessageRequest,
    csvType: CSVType,
): MessageRequest {
    return httpClient.post("$url/csv/${csvType.endpoint}") {
        contentType(ContentType.Application.Json)
        setBody(message)
    }.body()
}

// przykład GET
suspend fun fetchMessages(): List<MessageRequest> {
    return httpClient.post("$url/csv/PeKaOSA").body()
}
