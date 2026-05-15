package com.kontenery.config

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiEndpointsTest {
    @Test
    fun probeOrderIsDockerThenLocalhostThenRemote() {
        val candidates = orderedApiBaseUrlCandidates(
            dockerReachableUrl = "http://docker-host:8100",
            localhostUrl = "http://localhost:8100",
            remoteUrl = REMOTE_API_URL,
        )

        assertEquals(
            listOf(
                "http://docker-host:8100",
                "http://localhost:8100",
                REMOTE_API_URL,
            ),
            candidates,
        )
    }

    @Test
    fun probeOrderDeduplicatesIdenticalUrls() {
        val candidates = orderedApiBaseUrlCandidates(
            dockerReachableUrl = LOCALHOST_API_URL,
            localhostUrl = LOCALHOST_API_URL,
            remoteUrl = REMOTE_API_URL,
        )

        assertEquals(
            listOf(LOCALHOST_API_URL, REMOTE_API_URL),
            candidates,
        )
    }
}
