package com.kontenery.config

expect fun googleOAuthClientId(): String?

fun isGoogleSignInConfigured(): Boolean = !googleOAuthClientId().isNullOrBlank()
