package com.kontenery.config

import android.os.Build

private const val ANDROID_EMULATOR_API_URL = "http://10.0.2.2:8100"
private const val LOCAL_ANDROID_API_URL = "http://localhost:8100"
private const val REMOTE_API_URL = "http://217.154.148.172:8100"

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
    if (isAndroidEmulator()) ANDROID_EMULATOR_API_URL else REMOTE_API_URL

internal actual fun apiBaseUrlCandidates(): List<String> =
    if (isAndroidEmulator()) {
        listOf(ANDROID_EMULATOR_API_URL, LOCAL_ANDROID_API_URL, REMOTE_API_URL)
    } else {
        listOf(REMOTE_API_URL, ANDROID_EMULATOR_API_URL, LOCAL_ANDROID_API_URL)
    }

actual fun apiDeviceLabel(): String =
    if (isAndroidEmulator()) "emulator Android" else "telefon Android"
