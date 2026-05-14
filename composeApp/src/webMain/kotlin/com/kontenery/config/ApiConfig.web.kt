package com.kontenery.config

private const val LOCAL_WEB_API_URL = "http://localhost:8100"
private const val SAME_ORIGIN_API_PROXY_URL = "/api"

internal actual fun defaultApiBaseUrl(): String = LOCAL_WEB_API_URL

internal actual fun apiBaseUrlCandidates(): List<String> =
    listOf(
        LOCAL_WEB_API_URL,
        SAME_ORIGIN_API_PROXY_URL,
    )
