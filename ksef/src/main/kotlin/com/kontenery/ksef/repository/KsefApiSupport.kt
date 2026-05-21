package com.kontenery.ksef.repository

import com.kontenery.ksef.error.KsefApiException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

internal suspend inline fun <reified T> HttpResponse.requireBody(): T {
    if (!status.isSuccess()) {
        val body = runCatching { bodyAsText() }.getOrNull()
        throw KsefApiException(
            message = "KSeF API error ${status.value}: ${status.description}",
            statusCode = status.value,
            responseBody = body,
        )
    }
    return body()
}
