package com.kontenery.config

import android.os.Build

/** Emulator → host (Docker API na porcie 8100 na maszynie deweloperskiej). */
private const val ANDROID_EMULATOR_DOCKER_HOST_URL = "http://10.0.2.2:8100"

internal fun isAndroidEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk", ignoreCase = true)
        || Build.MODEL.contains("Emulator", ignoreCase = true)
        || Build.MODEL.contains("Android SDK built for x86", ignoreCase = true)
        || Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
        || Build.HARDWARE.contains("goldfish", ignoreCase = true)
        || Build.HARDWARE.contains("ranchu", ignoreCase = true)
        || Build.PRODUCT.contains("sdk_gphone", ignoreCase = true)

internal actual fun defaultApiBaseUrl(): String =
    apiBaseUrlCandidates().first()

internal actual fun apiBaseUrlCandidates(): List<String> =
    if (isAndroidEmulator()) {
        orderedApiBaseUrlCandidates(
            dockerReachableUrl = ANDROID_EMULATOR_DOCKER_HOST_URL,
            localhostUrl = LOCALHOST_API_URL,
        )
    } else {
        // Telefon: localhost:8100 po `adb reverse tcp:8100 tcp:8100` trafia do API w Dockerze na hoście.
        orderedApiBaseUrlCandidates(
            dockerReachableUrl = LOCALHOST_API_URL,
            localhostUrl = "http://127.0.0.1:8100",
        )
    }

actual fun apiDeviceLabel(): String =
    if (isAndroidEmulator()) "emulator Android" else "telefon Android"
