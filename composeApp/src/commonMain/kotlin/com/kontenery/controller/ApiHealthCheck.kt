package com.kontenery.controller

import com.kontenery.config.ApiConfig.baseUrl
import com.kontenery.config.ApiConfig.dockerUrl
import com.kontenery.config.ApiConfig.localUrl
import com.kontenery.config.ApiConfig.remoteUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ApiHealthCheck(
    private val httpClient: HttpClient
) {
    val urls = listOf(dockerUrl, localUrl, remoteUrl)
//    suspend fun healthCheck(): String = httpClient.get("$baseUrl/healthCheck").body()
    suspend fun healthCheck(): String {
        var currentIndex = 0
        repeat(urls.size) {
            val url = urls[currentIndex]
            try {
                val response = httpClient.get("$url/health")
                if (response.status.value in 200..299) {
                    baseUrl = url
                    return baseUrl
                } else throw IllegalStateException("Bad url")
            } catch (e: Throwable) {
                currentIndex = (currentIndex + 1) % urls.size
            }

        }
        throw IllegalStateException("All endpoints are down")
    }
}