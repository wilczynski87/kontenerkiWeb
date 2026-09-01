package com.kontenery.config

import kotlinx.browser.document

actual fun googleOAuthClientId(): String? =
    document.querySelector("meta[name='google-oauth-client-id']")
        ?.getAttribute("content")
        ?.trim()
        ?.takeUnless { it.isBlank() }
