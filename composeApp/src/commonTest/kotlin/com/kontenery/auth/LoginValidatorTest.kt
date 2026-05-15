package com.kontenery.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginValidatorTest {
    @Test
    fun acceptsDevCredentials() {
        val result = LoginValidator.validate(AuthDefaults.DEV_USERNAME, AuthDefaults.DEV_PASSWORD)
        assertTrue(result.isValid)
    }

    @Test
    fun rejectsEmptyUsername() {
        val result = LoginValidator.validate("", "ppp")
        assertFalse(result.isValid)
        assertEquals("Podaj login", result.usernameError)
    }

    @Test
    fun rejectsEmptyPassword() {
        val result = LoginValidator.validate("ppp", "")
        assertFalse(result.isValid)
        assertEquals("Podaj hasło", result.passwordError)
    }
}
