package com.kontenery.config

import com.kontenery.BuildConfig

actual fun googleOAuthClientId(): String? =
    BuildConfig.GOOGLE_OAUTH_CLIENT_ID.trim().takeUnless { it.isBlank() }
