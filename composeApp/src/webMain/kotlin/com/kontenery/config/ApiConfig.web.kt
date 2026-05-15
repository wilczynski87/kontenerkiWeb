package com.kontenery.config

private const val LOCAL_WEB_API_URL = "http://localhost:8100"
private const val SAME_ORIGIN_API_PROXY_URL = "/api"
private const val REMOTE_URL = "http://217.154.148.172:8100"

internal actual fun defaultApiBaseUrl(): String = LOCAL_WEB_API_URL

internal actual fun apiBaseUrlCandidates(): List<String> =
    listOf(
        LOCAL_WEB_API_URL,
        SAME_ORIGIN_API_PROXY_URL,
        REMOTE_URL,
    )
