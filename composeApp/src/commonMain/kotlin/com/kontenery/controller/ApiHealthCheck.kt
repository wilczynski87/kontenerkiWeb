package com.kontenery.controller

import com.kontenery.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class ApiHealthCheck(
    private val httpClient: HttpClient
) {
    suspend fun healthCheck(): String {
        for (url in ApiConfig.candidateBaseUrls) {
            try {
                val response = httpClient.get("$url/health")
                if (response.status.value in 200..299) {
                    ApiConfig.useBaseUrl(url)
                    return ApiConfig.baseUrl
                }
            } catch (e: Throwable) {
                // Try the next platform-specific candidate.
            }
        }
        throw IllegalStateException("All endpoints are down")
    }
}