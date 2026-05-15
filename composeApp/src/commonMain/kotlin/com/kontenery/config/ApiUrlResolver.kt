package com.kontenery.config

internal fun resolveHealthCheckUrl(baseUrl: String): String {
    val normalized = baseUrl.trimEnd('/')
    return when {
        normalized.startsWith("http://") || normalized.startsWith("https://") ->
            "$normalized/health"
        normalized.startsWith("/") ->
            "$normalized/health"
        else ->
            "$normalized/health"
    }
}
