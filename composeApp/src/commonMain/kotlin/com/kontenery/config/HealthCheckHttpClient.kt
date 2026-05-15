package com.kontenery.config

import io.ktor.client.HttpClient

internal expect fun createHealthCheckHttpClient(): HttpClient
