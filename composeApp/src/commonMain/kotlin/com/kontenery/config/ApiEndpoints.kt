package com.kontenery.config

/** Produkcyjny / zdalny backend (internet). */
internal const val REMOTE_API_URL = "http://217.154.148.172:8100"

/** API wystawione z kontenera Docker na hoście (mapowanie portu 8100). */
internal const val LOCALHOST_API_URL = "http://localhost:8100"

/**
 * Kolejność sprawdzania backendu przy logowaniu:
 * 1. Docker (URL zależny od platformy),
 * 2. localhost,
 * 3. internet (zdalny serwer).
 */
internal fun orderedApiBaseUrlCandidates(
    dockerReachableUrl: String,
    localhostUrl: String = LOCALHOST_API_URL,
    remoteUrl: String = REMOTE_API_URL,
): List<String> = listOf(dockerReachableUrl, localhostUrl, remoteUrl)
            .map { it.trimEnd('/') }
            .distinct()
