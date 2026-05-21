package com.kontenery.ksef.repository

import com.kontenery.ksef.config.KsefConfig
import com.kontenery.ksef.config.KsefEnvironment
import com.kontenery.ksef.http.KsefHttpClientFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class KsefAuthRepositoryTest {
    @Test
    fun fetchChallengeParsesResponse() = runBlocking {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/auth/challenge" -> respond(
                    content = """
                        {
                          "challenge": "20250625-CR-TEST",
                          "timestampMs": 1700000000000,
                          "timestamp": "2025-06-25T10:00:00Z"
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond("{}", HttpStatusCode.NotFound)
            }
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(KsefHttpClientFactory.json) }
        }
        val config = KsefConfig("5265877635", "token", KsefEnvironment.TEST)
        val repo = KsefAuthRepository(client, config, KsefSecurityRepository(client, config))

        val challenge = repo.fetchChallenge()
        assertEquals("20250625-CR-TEST", challenge.challenge)
        assertEquals(1700000000000L, challenge.timestampMs)

        client.close()
    }
}
