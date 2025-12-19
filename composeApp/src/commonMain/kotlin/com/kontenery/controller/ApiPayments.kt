package com.kontenery.controller

import com.kontenery.config.ApiConfig.BASE_URL
import com.kontenery.library.model.Payment
import com.kontenery.model.ClientOnList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApiPayments(
    private val httpClient: HttpClient
) {
    suspend fun getPaymentsForClient(
        clientId: Long,
        from: String?,
        to: String?
    ): List<Payment> = httpClient.get("$BASE_URL/payment/{clientId}/byClient") {
        parameter("clientId", clientId)
        parameter("from", from)
        parameter("to", to)
    }.body()

}