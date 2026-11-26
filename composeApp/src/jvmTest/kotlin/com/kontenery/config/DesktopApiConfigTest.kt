package com.kontenery.config

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopApiConfigTest {
    @Test
    fun desktopUsesOnlyRemoteServer() {
        assertEquals(listOf(REMOTE_API_URL.trimEnd('/')), apiBaseUrlCandidates())
    }
}
