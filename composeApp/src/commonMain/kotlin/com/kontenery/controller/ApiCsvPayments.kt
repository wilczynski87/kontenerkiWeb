package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.data.CSVType
import com.kontenery.data.MessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiCsvPayments(
    private val httpClient: HttpClient
) {
    suspend fun sendCSVMessage(
        message: MessageRequest,
        csvType: CSVType,
    ): MessageRequest {
        return httpClient.post("$baseUrl/csv/${csvType.endpoint}") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }.body()
    }

    // przykład GET
    suspend fun fetchMessages(
    ): List<MessageRequest> {
        return httpClient.post("$baseUrl/csv/PeKaOSA").body()
    }
}