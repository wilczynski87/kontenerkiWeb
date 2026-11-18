package com.kontenery.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UtilitiesInvoiceTest {
    @Test
    fun readingUsesSubmeterClientInsteadOfSubmeterId() {
        assertEquals(16L, clientIdForUtilityReading(submeterClientId = 16L, clientId = 5L))
    }

    @Test
    fun readingFallsBackToSelectedClient() {
        assertEquals(16L, clientIdForUtilityReading(submeterClientId = null, clientId = 16L))
    }

    @Test
    fun missingClientDoesNotBecomeZero() {
        assertNull(clientIdForUtilityReading(submeterClientId = null, clientId = null))
        assertNull(clientIdForUtilityReading(submeterClientId = 0L, clientId = null))
    }
}
