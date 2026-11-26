package com.kontenery.config

internal actual fun defaultApiBaseUrl(): String =
    apiBaseUrlCandidates().first()

/** Desktop łączy się z serwerem z SERVER_URI. Lokalny port 8100 zostaje dla Dockera. */
internal actual fun apiBaseUrlCandidates(): List<String> =
    listOf(REMOTE_API_URL.trimEnd('/'))

actual fun apiDeviceLabel(): String = "desktop"
