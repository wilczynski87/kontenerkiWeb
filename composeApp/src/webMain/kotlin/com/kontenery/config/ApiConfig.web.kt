package com.kontenery.config

/**
 * Proxy tej samej domeny (dev-server / nginx w Dockerze) → backend API.
 * Działa gdy webpack/nginx kieruje `/api` na lokalny kontener.
 */
private const val SAME_ORIGIN_DOCKER_PROXY_URL = "/api"

internal actual fun defaultApiBaseUrl(): String =
    apiBaseUrlCandidates().first()

internal actual fun apiBaseUrlCandidates(): List<String> =
    orderedApiBaseUrlCandidates(
        dockerReachableUrl = SAME_ORIGIN_DOCKER_PROXY_URL,
        localhostUrl = LOCALHOST_API_URL,
    )

actual fun apiDeviceLabel(): String = "przeglądarka"
