package com.kontenery.ksef.error

open class KsefException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class KsefValidationException(message: String) : KsefException(message)

class KsefApiException(
    message: String,
    val statusCode: Int? = null,
    val responseBody: String? = null,
    cause: Throwable? = null,
) : KsefException(message, cause)

class KsefAuthenticationException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null,
) : KsefException(message, cause)
