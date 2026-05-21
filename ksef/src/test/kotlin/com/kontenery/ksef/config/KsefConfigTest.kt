package com.kontenery.ksef.config

import com.kontenery.ksef.error.KsefValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KsefConfigTest {
    @Test
    fun normalizeTokenUsesSecretAfterPipe() {
        assertEquals(
            "secret-part",
            KsefConfig.normalizeKsefToken("20250625-TK-REF|secret-part"),
        )
    }

    @Test
    fun validatedStripsNonDigitsFromNip() {
        val config = KsefConfig(nip = "526-587-76-35", ksefToken = "abc").validated()
        assertEquals("5265877635", config.nip)
    }

    @Test
    fun validatedRejectsInvalidNip() {
        assertFailsWith<KsefValidationException> {
            KsefConfig(nip = "123", ksefToken = "x").validated()
        }
    }
}
