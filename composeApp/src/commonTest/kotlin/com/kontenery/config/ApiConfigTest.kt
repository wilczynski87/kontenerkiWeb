package com.kontenery.config

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiConfigTest {
    @Test
    fun candidateBaseUrlsAreNormalizedAndDoNotContainPublicIp() {
        val candidates = ApiConfig.candidateBaseUrls

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.all { it == it.trimEnd('/') })
        assertFalse(candidates.any { it.contains("217.154.148.172") })
    }
}
