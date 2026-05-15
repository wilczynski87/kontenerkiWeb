package com.kontenery.controller

import com.kontenery.config.ApiConfig
import com.kontenery.config.createHealthCheckHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get

data class HealthCheckResult(
    val activeBaseUrl: String?,
    val triedUrls: List<String>,
    val lastError: String? = null,
)

class ApiHealthCheck(
    private val httpClient: HttpClient = createHealthCheckHttpClient(),
) {
    suspend fun healthCheck(onProbe: (suspend (String) -> Unit)? = null): HealthCheckResult {
        val triedUrls = mutableListOf<String>()
        var lastError: String? = null
        for (url in ApiConfig.candidateBaseUrls) {
            triedUrls.add(url)
            onProbe?.invoke(url)
            try {
                val response = httpClient.get("$url/health")
                if (response.status.value in 200..299) {
                    ApiConfig.useBaseUrl(url)
                    return HealthCheckResult(activeBaseUrl = url, triedUrls = triedUrls)
                }
                lastError = "HTTP ${response.status.value} dla $url/health"
            } catch (e: Throwable) {
                lastError = "${e::class.simpleName}: ${e.message ?: "brak komunikatu"}"
            }
        }
        return HealthCheckResult(activeBaseUrl = null, triedUrls = triedUrls, lastError = lastError)
    }
}
