package com.kontenery.config

private const val ANDROID_EMULATOR_API_URL = "http://10.0.2.2:8100"
private const val LOCAL_ANDROID_API_URL = "http://localhost:8100"
private const val REMOTE_URL = "http://217.154.148.172:8100"

internal actual fun defaultApiBaseUrl(): String = ANDROID_EMULATOR_API_URL

internal actual fun apiBaseUrlCandidates(): List<String> =
    listOf(
        ANDROID_EMULATOR_API_URL,
        LOCAL_ANDROID_API_URL,
        REMOTE_URL,
    )
