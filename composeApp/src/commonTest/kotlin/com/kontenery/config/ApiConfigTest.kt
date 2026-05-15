package com.kontenery.config

import kotlin.test.Test
import kotlin.test.assertTrue

class ApiConfigTest {
    @Test
    fun candidateBaseUrlsAreNormalized() {
        val candidates = ApiConfig.candidateBaseUrls

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.all { it == it.trimEnd('/') })
    }
}
