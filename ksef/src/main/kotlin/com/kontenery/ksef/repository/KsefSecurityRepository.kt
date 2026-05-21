package com.kontenery.ksef.repository

import com.kontenery.ksef.config.KsefConfig
import com.kontenery.ksef.dto.PublicKeyCertificate
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class KsefSecurityRepository(
    private val httpClient: HttpClient,
    private val config: KsefConfig,
) {
    suspend fun fetchPublicKeyCertificates(): List<PublicKeyCertificate> =
        httpClient.get("${config.baseUrl}/security/public-key-certificates")
            .requireBody()
}
