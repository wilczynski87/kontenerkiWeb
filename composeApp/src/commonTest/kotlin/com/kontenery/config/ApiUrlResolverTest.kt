package com.kontenery.config

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiUrlResolverTest {
    @Test
    fun buildsAbsoluteHealthUrl() {
        assertEquals(
            "$REMOTE_API_URL/health",
            resolveHealthCheckUrl(REMOTE_API_URL),
        )
    }

    @Test
    fun buildsRelativeProxyHealthUrl() {
        assertEquals("/api/health", resolveHealthCheckUrl("/api"))
    }
}
