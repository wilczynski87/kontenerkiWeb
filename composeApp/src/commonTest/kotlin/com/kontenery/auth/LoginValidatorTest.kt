package com.kontenery.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginValidatorTest {
    @Test
    fun acceptsNonEmptyCredentials() {
        val result = LoginValidator.validate("user", "secret")
        assertTrue(result.isValid)
    }

    @Test
    fun rejectsEmptyUsername() {
        val result = LoginValidator.validate("", "secret")
        assertFalse(result.isValid)
        assertEquals("Podaj login", result.usernameError)
    }

    @Test
    fun rejectsEmptyPassword() {
        val result = LoginValidator.validate("user", "")
        assertFalse(result.isValid)
        assertEquals("Podaj hasło", result.passwordError)
    }
}
