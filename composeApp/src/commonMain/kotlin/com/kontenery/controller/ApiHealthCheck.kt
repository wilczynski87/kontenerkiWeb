package com.kontenery.controller

import com.kontenery.config.ApiConfig
import com.kontenery.config.createHealthCheckHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get

data class HealthCheckResult(
    val activeBaseUrl: String?,
    val triedUrls: List<String>,
)

class ApiHealthCheck(
    private val httpClient: HttpClient = createHealthCheckHttpClient(),
) {
    suspend fun healthCheck(onProbe: (suspend (String) -> Unit)? = null): HealthCheckResult {
        val triedUrls = mutableListOf<String>()
        for (url in ApiConfig.candidateBaseUrls) {
            triedUrls.add(url)
            onProbe?.invoke(url)
            try {
                val response = httpClient.get("$url/health")
                if (response.status.value in 200..299) {
                    ApiConfig.useBaseUrl(url)
                    return HealthCheckResult(activeBaseUrl = url, triedUrls = triedUrls)
                }
            } catch (_: Throwable) {
                // Try next candidate.
            }
        }
        return HealthCheckResult(activeBaseUrl = null, triedUrls = triedUrls)
    }
}
