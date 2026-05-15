package com.kontenery.config

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiUrlResolverTest {
    @Test
    fun buildsAbsoluteHealthUrl() {
        assertEquals(
            "http://217.154.148.172:8100/health",
            resolveHealthCheckUrl("http://217.154.148.172:8100"),
        )
    }

    @Test
    fun buildsRelativeProxyHealthUrl() {
        assertEquals("/api/health", resolveHealthCheckUrl("/api"))
    }
}
