package com.kontenery.config

object ApiConfig {
    var baseUrl: String = defaultApiBaseUrl()
        private set

    val candidateBaseUrls: List<String>
        get() = apiBaseUrlCandidates()
            .map { it.trimEnd('/') }
            .distinct()

    fun useBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }
}

internal expect fun defaultApiBaseUrl(): String

internal expect fun apiBaseUrlCandidates(): List<String>

/** Etykieta platformy na ekranie logowania (debug). */
expect fun apiDeviceLabel(): String