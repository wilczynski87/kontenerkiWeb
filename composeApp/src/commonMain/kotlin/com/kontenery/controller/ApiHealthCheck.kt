package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ApiHealthCheck(private val httpClient: HttpClient) {
    suspend fun healthCheck(): String = httpClient.get("$baseUrl/healthCheck").body()
}