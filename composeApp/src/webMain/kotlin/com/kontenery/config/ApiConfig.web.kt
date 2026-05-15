package com.kontenery.config

private const val REMOTE_API_URL = "http://217.154.148.172:8100"
private const val LOCAL_WEB_API_URL = "http://localhost:8100"
/** Działa tylko gdy dev-server / nginx ma proxy `/api` → backend (patrz composeApp/build.gradle.kts). */
private const val SAME_ORIGIN_API_PROXY_URL = "/api"

internal actual fun defaultApiBaseUrl(): String = REMOTE_API_URL

internal actual fun apiBaseUrlCandidates(): List<String> =
    listOf(
        REMOTE_API_URL,
        LOCAL_WEB_API_URL,
        SAME_ORIGIN_API_PROXY_URL,
    )

actual fun apiDeviceLabel(): String = "przeglądarka"
