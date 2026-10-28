package com.kontenery.config

internal actual fun defaultApiBaseUrl(): String =
    apiBaseUrlCandidates().first()

internal actual fun apiBaseUrlCandidates(): List<String> =
    orderedApiBaseUrlCandidates(
        dockerReachableUrl = LOCALHOST_API_URL,
        localhostUrl = "http://127.0.0.1:8100",
    )

actual fun apiDeviceLabel(): String = "desktop"
